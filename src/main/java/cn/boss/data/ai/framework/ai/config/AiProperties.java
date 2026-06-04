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

    @Data
    public static class WebSearch {
        private boolean enable;
        private String apiKey;
    }

}
