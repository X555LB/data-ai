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
     * 谷歌 Gemini
     */
    private Gemini gemini;

    /**
     * 字节豆包
     */
    private DouBao doubao;

    /**
     * 腾讯混元
     */
    private HunYuan hunyuan;

    /**
     * 硅基流动
     */
    private SiliconFlow siliconflow;

    /**
     * 讯飞星火
     */
    private XingHuo xinghuo;

    /**
     * 百川
     */
    private BaiChuan baichuan;

    /**
     * Grok
     */
    private Grok grok;

    /**
     * 网络搜索
     */
    private WebSearch webSearch;

    @Data
    public static class Gemini {
        private String enable;
        private String apiKey;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
    }

    @Data
    public static class DouBao {
        private String enable;
        private String apiKey;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
    }

    @Data
    public static class HunYuan {
        private String enable;
        private String baseUrl;
        private String apiKey;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
    }

    @Data
    public static class SiliconFlow {
        private String enable;
        private String apiKey;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
    }

    @Data
    public static class XingHuo {
        private String enable;
        private String appId;
        private String appKey;
        private String secretKey;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
    }

    @Data
    public static class BaiChuan {
        private String enable;
        private String apiKey;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
    }

    @Data
    public static class Grok {
        private String enable;
        private String apiKey;
        private String baseUrl;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
    }

    @Data
    public static class WebSearch {
        private boolean enable;
        private String apiKey;
    }

}
