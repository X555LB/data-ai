package cn.boss.data.ai.framework.ai.core.valuation;

import cn.boss.data.ai.framework.common.pojo.CommonResult;
import cn.boss.data.ai.framework.common.util.json.JsonUtils;
import cn.boss.data.ai.service.llm.vo.AiProductKnowledgeRecord;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 估值后端 API 调用客户端
 */
@Slf4j
public class AiValuationClient {

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final String baseUrl;
    private final String apiKey;

    public AiValuationClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    private static final TypeReference<CommonResult<List<AiProductKnowledgeRecord>>> LIST_TYPE_REF =
            new TypeReference<>() {};

    /**
     * 获取所有产品知识列表（market 市场价格交易库 + his 历史经验交易库，合并返回）
     *
     * @return 产品知识记录列表
     */
    public List<AiProductKnowledgeRecord> getAllProductKnowledgeList() {
        try {
            String url = baseUrl + "/biz/valuation/knowledge/all-list";
            String responseBody = StrUtil.isNotEmpty(apiKey)
                    ? HttpUtil.createGet(url).header(INTERNAL_API_KEY_HEADER, apiKey).timeout(30000).execute().body()
                    : HttpUtil.get(url, 30000);

            if (!JsonUtils.isJson(responseBody)) {
                log.error("[getAllProductKnowledgeList] 估值后端返回非 JSON 响应（可能服务未启动或地址错误），url={}，response={}",
                        url, StrUtil.sub(responseBody, 0, 200));
                return Collections.emptyList();
            }

            CommonResult<List<AiProductKnowledgeRecord>> result = JsonUtils.parseObject(responseBody, LIST_TYPE_REF);
            if (!result.isSuccess()) {
                log.warn("[getAllProductKnowledgeList] 估值后端返回错误：{}", responseBody);
                return Collections.emptyList();
            }

            List<AiProductKnowledgeRecord> records = result.getData();
            if (CollUtil.isEmpty(records)) {
                return Collections.emptyList();
            }

            log.info("[getAllProductKnowledgeList] 共获取 {} 条产品知识记录（market + his）", records.size());
            return records;
        } catch (Exception e) {
            log.error("[getAllProductKnowledgeList] 调用估值后端失败", e);
            return Collections.emptyList();
        }
    }

}
