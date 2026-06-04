package cn.boss.data.ai.enums.model;

import cn.boss.data.ai.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 模型平台
 */
@Getter
@AllArgsConstructor
public enum AiPlatformEnum implements ArrayValuable<String> {

    // ========== 国内平台 ==========

    TONG_YI("TongYi", "通义千问"), // 阿里
    DEEP_SEEK("DeepSeek", "DeepSeek"), // DeepSeek

    // ========== 国外平台 ==========

    OPENAI("OpenAI", "OpenAI"), // OpenAI 官方
    OLLAMA("Ollama", "Ollama"),

    ;

    /**
     * 平台
     */
    private final String platform;
    /**
     * 平台名
     */
    private final String name;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiPlatformEnum::getPlatform).toArray(String[]::new);

    public static AiPlatformEnum validatePlatform(String platform) {
        for (AiPlatformEnum platformEnum : AiPlatformEnum.values()) {
            if (platformEnum.getPlatform().equals(platform)) {
                return platformEnum;
            }
        }
        throw new IllegalArgumentException("非法平台： " + platform);
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
