package cn.boss.data.ai.framework.ai.core.websearch;

/**
 * 网络搜索客户端接口
 */
public interface AiWebSearchClient {

    /**
     * 网页搜索
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    AiWebSearchResponse search(AiWebSearchRequest request);

}
