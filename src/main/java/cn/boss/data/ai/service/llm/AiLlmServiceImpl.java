package cn.boss.data.ai.service.llm;

import cn.boss.data.ai.controller.llm.vo.AiFieldAnalyzeReqVO;
import cn.boss.data.ai.controller.llm.vo.AiLlmSearchReqVO;
import cn.boss.data.ai.enums.model.AiPlatformEnum;
import cn.boss.data.ai.framework.ai.core.model.AiModelFactory;
import cn.boss.data.ai.framework.ai.core.valuation.AiValuationClient;
import cn.boss.data.ai.service.llm.vo.AiProductKnowledgeRecord;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * LLM 服务实现
 */
@Service
@Validated
@Slf4j
public class AiLlmServiceImpl implements AiLlmService {

    @Resource
    private AiModelFactory modelFactory;

    @Resource
    private AiValuationClient valuationClient;

    // ========== Prompt 模板 ==========

    private static final String SEARCH_SYSTEM_PROMPT = """
            你是一个产品匹配助手。用户会提供一组产品列表（包含产品ID和产品名称），\
            以及一个搜索关键词。请根据语义相似度和关键词匹配，从产品列表中找出与关键词最匹配的产品。

            匹配规则：
            1. 任一字段包含查询词或语义相关即可匹配
            2. 忽略地域、企业名称等修饰词
            3. 按匹配度从高到低排序

            输出规则（严格遵守）：
            1. 只输出匹配的产品ID，用英文逗号分隔，例如：123456,654321,789012
            2. 如果没有匹配的产品，只输出一个字：空
            3. 不要输出任何解释、标点或额外文字""";

    private static final String FIELD_ANALYZE_SYSTEM_PROMPT = """
            你是一个数据字段重要性分析专家。根据给定的产品信息和使用场景，\
            分析每个字段对该产品业务的重要程度。

            判断标准：
            - important（重要）：对产品核心业务起到决定性作用的字段
            - general（一般）：仅辅助说明或对产品业务无直接效用的字段

            输出规则（严格遵守）：
            1. 返回 JSON 数组，每个元素包含 id、name、level、reason 四个字段
            2. level 取值只能是 "important" 或 "general"
            3. reason 简要说明该字段对产品业务的作用（50字以内）
            4. 只输出 JSON 数组，不要输出任何解释、markdown 标记或额外文字
            5. 确保输出是合法的 JSON 格式""";

    // 用于从 LLM 返回中提取数字 ID 的正则
    private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String llmSearch(AiLlmSearchReqVO reqVO) {
        // 1. 调用估值后端获取产品知识列表
        List<AiProductKnowledgeRecord> records = valuationClient.getProductKnowledgeList();
        if (CollUtil.isEmpty(records)) {
            return "空";
        }

        // 2. 按条件过滤
        if (Boolean.TRUE.equals(reqVO.getFilterPrice())) {
            records = records.stream()
                    .filter(r -> r.getProdPrice() != null && r.getProdPrice().compareTo(BigDecimal.ZERO) > 0)
                    .toList();
        }
        if (Boolean.TRUE.equals(reqVO.getFilterBlocked())) {
            records = records.stream()
                    .filter(r -> !"T".equals(r.getBlockFlag()))
                    .toList();
        }
        if (CollUtil.isEmpty(records)) {
            return "空";
        }

        // 3. 构建产品列表文本（ID + 名称 + 相关信息）
        String productListText = records.stream()
                .map(r -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ID:").append(r.getId());
                    sb.append(" 名称:").append(StrUtil.blankToDefault(r.getProductName(), ""));
                    if (StrUtil.isNotBlank(r.getInfo())) {
                        sb.append(" 信息:").append(r.getInfo());
                    }
                    if (StrUtil.isNotBlank(r.getSectorsName())) {
                        sb.append(" 场景:").append(r.getSectorsName());
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));

        // 4. 调用 LLM 语义匹配
        ChatModel chatModel = modelFactory.getDefaultChatModel(AiPlatformEnum.DEEP_SEEK);
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SEARCH_SYSTEM_PROMPT),
                new UserMessage("搜索关键词：" + reqVO.getKeyword()
                        + "\n\n产品列表（共" + records.size() + "条）：\n" + productListText)
        ));

        ChatResponse response = chatModel.call(prompt);
        String result = response.getResult().getOutput().getText();
        log.info("[llmSearch] LLM 原始返回：{}", result);

        // 5. 后处理：提取 ID 列表
        if (StrUtil.isBlank(result) || result.contains("空")) {
            return "空";
        }
        // 用正则提取所有数字 ID
        Matcher matcher = ID_PATTERN.matcher(result);
        StringBuilder ids = new StringBuilder();
        while (matcher.find()) {
            if (!ids.isEmpty()) {
                ids.append(",");
            }
            ids.append(matcher.group());
        }

        return ids.isEmpty() ? "空" : ids.toString();
    }

    @Override
    public String fieldAnalyzeByLlm(AiFieldAnalyzeReqVO reqVO) {
        // 1. 构建 User Message
        String userMessage = buildFieldAnalyzeUserMessage(reqVO);

        // 2. 调用 LLM 分析
        ChatModel chatModel = modelFactory.getDefaultChatModel(AiPlatformEnum.DEEP_SEEK);
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(FIELD_ANALYZE_SYSTEM_PROMPT),
                new UserMessage(userMessage)
        ));

        ChatResponse response = chatModel.call(prompt);
        String result = response.getResult().getOutput().getText();
        log.info("[fieldAnalyzeByLlm] LLM 原始返回：{}", result);

        // 3. 后处理：清理 markdown 标记 + 校验 JSON
        if (StrUtil.isBlank(result)) {
            return "[]";
        }
        result = result.trim();
        // 移除可能的 markdown 代码块标记
        if (result.startsWith("```")) {
            result = result.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        }
        // 校验 JSON 格式
        try {
            OBJECT_MAPPER.readTree(result);
        } catch (Exception e) {
            log.warn("[fieldAnalyzeByLlm] LLM 返回的不是合法 JSON：{}", result);
            return "[]";
        }

        return result;
    }

    private String buildFieldAnalyzeUserMessage(AiFieldAnalyzeReqVO reqVO) {
        StringBuilder sb = new StringBuilder();
        sb.append("产品名称：").append(reqVO.getProductName()).append("\n");
        if (StrUtil.isNotBlank(reqVO.getProductIntro())) {
            sb.append("产品简介：").append(reqVO.getProductIntro()).append("\n");
        }
        if (StrUtil.isNotBlank(reqVO.getUsageScenario())) {
            sb.append("使用场景：").append(reqVO.getUsageScenario()).append("\n");
        }
        sb.append("\n字段列表：\n").append(reqVO.getFields());
        return sb.toString();
    }

}
