package cn.boss.data.ai.framework.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 配置属性类
 */
@ConfigurationProperties(prefix = "boss.ai")
@Data
public class AiProperties {

    /**
     * 网络搜索
     */
    private WebSearch webSearch;

    /**
     * 估值系统
     */
    private Valuation valuation;

    @Data
    public static class WebSearch {
        private boolean enable;
        private String apiKey;
    }

    @Data
    public static class Valuation {
        /**
         * 估值后端基础 URL
         */
        private String baseUrl;
    }

}
