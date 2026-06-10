package cn.boss.data.ai.service.llm;

import cn.boss.data.ai.controller.llm.vo.AiFieldAnalyzeReqVO;
import cn.boss.data.ai.controller.llm.vo.AiLlmSearchReqVO;

/**
 * LLM 服务接口
 */
public interface AiLlmService {

    /**
     * 产品语义搜索
     *
     * @param reqVO 搜索请求
     * @return 逗号分隔的产品 ID 字符串，无匹配时返回"空"
     */
    String llmSearch(AiLlmSearchReqVO reqVO);

    /**
     * 字段重要性分析
     *
     * @param reqVO 分析请求
     * @return JSON 数组字符串，包含每个字段的 level 和 reason
     */
    String fieldAnalyzeByLlm(AiFieldAnalyzeReqVO reqVO);

}
