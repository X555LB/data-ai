package cn.boss.data.ai.framework.ai.core.valuation;

import cn.boss.data.ai.service.llm.vo.AiProductKnowledgeRecord;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 估值后端 API 调用客户端
 */
@Slf4j
public class AiValuationClient {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_RECORDS = 500;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String baseUrl;

    public AiValuationClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 获取产品知识列表（分页拉取，最多 MAX_RECORDS 条）
     *
     * @return 产品知识记录列表
     */
    public List<AiProductKnowledgeRecord> getProductKnowledgeList() {
        List<AiProductKnowledgeRecord> allRecords = new ArrayList<>();
        int pageNo = 1;

        while (allRecords.size() < MAX_RECORDS) {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("pageNo", pageNo);
                params.put("pageSize", PAGE_SIZE);

                String url = baseUrl + "/biz/knowledge/product-knowledge/page";
                String responseBody = HttpUtil.get(url, params, 30000);

                JsonNode root = OBJECT_MAPPER.readTree(responseBody);
                JsonNode codeNode = root.get("code");
                if (codeNode != null && codeNode.asInt() != 0) {
                    log.warn("[getProductKnowledgeList] 估值后端返回错误：{}", responseBody);
                    break;
                }

                JsonNode dataNode = root.get("data");
                if (dataNode == null) {
                    break;
                }

                JsonNode listNode = dataNode.get("list");
                if (listNode == null || !listNode.isArray() || listNode.isEmpty()) {
                    break;
                }

                List<AiProductKnowledgeRecord> records = OBJECT_MAPPER.readValue(
                        listNode.traverse(),
                        OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, AiProductKnowledgeRecord.class)
                );
                if (CollUtil.isEmpty(records)) {
                    break;
                }

                allRecords.addAll(records);

                JsonNode totalNode = dataNode.get("total");
                int total = totalNode != null ? totalNode.asInt() : 0;
                if (allRecords.size() >= total) {
                    break;
                }
                pageNo++;
            } catch (Exception e) {
                log.error("[getProductKnowledgeList] 调用估值后端失败，pageNo={}", pageNo, e);
                break;
            }
        }

        log.info("[getProductKnowledgeList] 共获取 {} 条产品知识记录", allRecords.size());
        return allRecords;
    }

}
