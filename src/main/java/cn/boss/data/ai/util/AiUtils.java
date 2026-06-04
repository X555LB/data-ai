package cn.boss.data.ai.util;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.boss.data.ai.enums.model.AiPlatformEnum;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;

import java.util.*;

/**
 * Spring AI 工具类
 */
public class AiUtils {

    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens) {
        return buildChatOptions(platform, model, temperature, maxTokens, null, null);
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens,
                                               List<ToolCallback> toolCallbacks, Map<String, Object> toolContext) {
        toolCallbacks = ObjUtil.defaultIfNull(toolCallbacks, Collections.emptyList());
        toolContext = ObjUtil.defaultIfNull(toolContext, Collections.emptyMap());
        switch (platform) {
            case TONG_YI:
                return DashScopeChatOptions.builder().withModel(model).withTemperature(temperature).withMaxToken(maxTokens)
                        .withEnableThinking(true)
                        .withToolCallbacks(toolCallbacks).withToolContext(toolContext).build();
            case DEEP_SEEK:
                return DeepSeekChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case OPENAI:
                return OpenAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case OLLAMA:
                return OllamaChatOptions.builder().model(model).temperature(temperature).numPredict(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    public static Message buildMessage(String type, String content) {
        if (MessageType.USER.getValue().equals(type)) {
            return new UserMessage(content);
        }
        if (MessageType.ASSISTANT.getValue().equals(type)) {
            return new AssistantMessage(content);
        }
        if (MessageType.SYSTEM.getValue().equals(type)) {
            return new SystemMessage(content);
        }
        if (MessageType.TOOL.getValue().equals(type)) {
            throw new UnsupportedOperationException("暂不支持 tool 消息：" + content);
        }
        throw new IllegalArgumentException(StrUtil.format("未知消息类型({})", type));
    }

    /**
     * 构建通用工具上下文（已移除租户和登录用户信息）
     */
    public static Map<String, Object> buildCommonToolContext() {
        return new HashMap<>();
    }

    @SuppressWarnings("ConstantValue")
    public static String getChatResponseContent(ChatResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {
            return null;
        }
        return response.getResult().getOutput().getText();
    }

    @SuppressWarnings("ConstantValue")
    public static String getChatResponseReasoningContent(ChatResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {
            return null;
        }
        if (response.getResult().getOutput() instanceof DeepSeekAssistantMessage) {
            return ((DeepSeekAssistantMessage) (response.getResult().getOutput())).getReasoningContent();
        }
        return null;
    }

}
