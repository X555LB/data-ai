package cn.boss.data.ai.framework.ai.config;

import cn.hutool.core.util.StrUtil;
import cn.boss.data.ai.framework.common.util.spring.SpringUtils;
import cn.boss.data.ai.framework.ai.core.model.AiModelFactory;
import cn.boss.data.ai.framework.ai.core.model.AiModelFactoryImpl;
import cn.boss.data.ai.framework.ai.core.model.baichuan.BaiChuanChatModel;
import cn.boss.data.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import cn.boss.data.ai.framework.ai.core.model.gemini.GeminiChatModel;
import cn.boss.data.ai.framework.ai.core.model.grok.GrokChatModel;
import cn.boss.data.ai.framework.ai.core.model.hunyuan.HunYuanChatModel;
import cn.boss.data.ai.framework.ai.core.model.siliconflow.SiliconFlowApiConstants;
import cn.boss.data.ai.framework.ai.core.model.siliconflow.SiliconFlowChatModel;
import cn.boss.data.ai.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.boss.data.ai.framework.ai.core.websearch.AiWebSearchClient;
import cn.boss.data.ai.framework.ai.core.websearch.bocha.AiBoChaWebSearchClient;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * AI 自动配置
 */
@Configuration
@EnableConfigurationProperties({ AiProperties.class,
        QdrantVectorStoreProperties.class,
        RedisVectorStoreProperties.class
})
@Slf4j
public class AiAutoConfiguration {

    @Bean
    public AiModelFactory aiModelFactory() {
        return new AiModelFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.NOOP;
    }

    // ========== 各种 AI Client 创建 ==========

    @Bean
    @ConditionalOnProperty(value = "boss.ai.gemini.enable", havingValue = "true")
    public GeminiChatModel geminiChatModel(AiProperties aiProperties) {
        AiProperties.Gemini properties = aiProperties.getGemini();
        return buildGeminiChatClient(properties);
    }

    public GeminiChatModel buildGeminiChatClient(AiProperties.Gemini properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(GeminiChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(GeminiChatModel.BASE_URL)
                        .completionsPath(GeminiChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new GeminiChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "boss.ai.doubao.enable", havingValue = "true")
    public DouBaoChatModel douBaoChatClient(AiProperties aiProperties) {
        AiProperties.DouBao properties = aiProperties.getDoubao();
        return buildDouBaoChatClient(properties);
    }

    public DouBaoChatModel buildDouBaoChatClient(AiProperties.DouBao properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(DouBaoChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(DouBaoChatModel.BASE_URL)
                        .completionsPath(DouBaoChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new DouBaoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "boss.ai.siliconflow.enable", havingValue = "true")
    public SiliconFlowChatModel siliconFlowChatClient(AiProperties aiProperties) {
        AiProperties.SiliconFlow properties = aiProperties.getSiliconflow();
        return buildSiliconFlowChatClient(properties);
    }

    public SiliconFlowChatModel buildSiliconFlowChatClient(AiProperties.SiliconFlow properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(SiliconFlowApiConstants.MODEL_DEFAULT);
        }
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(SiliconFlowApiConstants.DEFAULT_BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new SiliconFlowChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "boss.ai.hunyuan.enable", havingValue = "true")
    public HunYuanChatModel hunYuanChatClient(AiProperties aiProperties) {
        AiProperties.HunYuan properties = aiProperties.getHunyuan();
        return buildHunYuanChatClient(properties);
    }

    public HunYuanChatModel buildHunYuanChatClient(AiProperties.HunYuan properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(HunYuanChatModel.MODEL_DEFAULT);
        }
        if (StrUtil.isEmpty(properties.getBaseUrl())) {
            properties.setBaseUrl(
                    StrUtil.startWithIgnoreCase(properties.getModel(), "deepseek") ? HunYuanChatModel.DEEP_SEEK_BASE_URL
                            : HunYuanChatModel.BASE_URL);
        }
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(properties.getBaseUrl())
                        .completionsPath(HunYuanChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new HunYuanChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "boss.ai.xinghuo.enable", havingValue = "true")
    public XingHuoChatModel xingHuoChatClient(AiProperties aiProperties) {
        AiProperties.XingHuo properties = aiProperties.getXinghuo();
        return buildXingHuoChatClient(properties);
    }

    public XingHuoChatModel buildXingHuoChatClient(AiProperties.XingHuo properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(XingHuoChatModel.MODEL_DEFAULT);
        }
        OpenAiApi.Builder builder = OpenAiApi.builder()
                .baseUrl(XingHuoChatModel.BASE_URL_V1)
                .apiKey(properties.getAppKey() + ":" + properties.getSecretKey());
        if ("x1".equals(properties.getModel())) {
            builder.baseUrl(XingHuoChatModel.BASE_URL_V2)
                    .completionsPath(XingHuoChatModel.BASE_COMPLETIONS_PATH_V2);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(builder.build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new XingHuoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "boss.ai.baichuan.enable", havingValue = "true")
    public BaiChuanChatModel baiChuanChatClient(AiProperties aiProperties) {
        AiProperties.BaiChuan properties = aiProperties.getBaichuan();
        return buildBaiChuanChatClient(properties);
    }

    public BaiChuanChatModel buildBaiChuanChatClient(AiProperties.BaiChuan properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(BaiChuanChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(BaiChuanChatModel.BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new BaiChuanChatModel(openAiChatModel);
    }

    public ChatModel buildGrokChatClient(AiProperties.Grok properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(GrokChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(Optional.ofNullable(properties.getBaseUrl())
                                .orElse(GrokChatModel.BASE_URL))
                        .completionsPath(GrokChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new GrokChatModel(openAiChatModel);
    }

    // ========== RAG 相关 ==========

    @Bean
    public TokenCountEstimator tokenCountEstimator() {
        return new JTokkitTokenCountEstimator();
    }

    @Bean
    public BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy();
    }

    private static ToolCallingManager getToolCallingManager() {
        return SpringUtils.getBean(ToolCallingManager.class);
    }

    // ========== Web Search 相关 ==========

    @Bean
    @ConditionalOnProperty(value = "boss.ai.web-search.enable", havingValue = "true")
    public AiWebSearchClient webSearchClient(AiProperties aiProperties) {
        return new AiBoChaWebSearchClient(aiProperties.getWebSearch().getApiKey());
    }

}
