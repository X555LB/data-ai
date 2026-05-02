package cn.boss.data.ai.framework.ai.core.model;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.boss.data.ai.framework.common.util.spring.SpringUtils;
import cn.boss.data.ai.enums.model.AiPlatformEnum;
import cn.boss.data.ai.framework.ai.config.AiAutoConfiguration;
import cn.boss.data.ai.framework.ai.config.AiProperties;
import cn.boss.data.ai.framework.ai.core.model.baichuan.BaiChuanChatModel;
import cn.boss.data.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import cn.boss.data.ai.framework.ai.core.model.gemini.GeminiChatModel;
import cn.boss.data.ai.framework.ai.core.model.hunyuan.HunYuanChatModel;
import cn.boss.data.ai.framework.ai.core.model.siliconflow.SiliconFlowApiConstants;
import cn.boss.data.ai.framework.ai.core.model.siliconflow.SiliconFlowChatModel;
import cn.boss.data.ai.framework.ai.core.model.xinghuo.XingHuoChatModel;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeEmbeddingAutoConfiguration;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import io.micrometer.observation.ObservationRegistry;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.SneakyThrows;
import org.springaicommunity.moonshot.MoonshotChatModel;
import org.springaicommunity.moonshot.MoonshotChatOptions;
import org.springaicommunity.moonshot.api.MoonshotApi;
import org.springaicommunity.moonshot.autoconfigure.MoonshotChatAutoConfiguration;
import org.springaicommunity.qianfan.QianFanChatModel;
import org.springaicommunity.qianfan.QianFanEmbeddingModel;
import org.springaicommunity.qianfan.QianFanEmbeddingOptions;
import org.springaicommunity.qianfan.api.QianFanApi;
import org.springaicommunity.qianfan.autoconfigure.QianFanChatAutoConfiguration;
import org.springaicommunity.qianfan.autoconfigure.QianFanEmbeddingAutoConfiguration;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiChatAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiEmbeddingProperties;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration;
import org.springframework.ai.model.minimax.autoconfigure.MiniMaxChatAutoConfiguration;
import org.springframework.ai.model.minimax.autoconfigure.MiniMaxEmbeddingAutoConfiguration;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiChatAutoConfiguration;
import org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiEmbeddingAutoConfiguration;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.observation.DefaultVectorStoreObservationConvention;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.ai.zhipuai.*;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import redis.clients.jedis.JedisPooled;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertList;
import static org.springframework.ai.retry.RetryUtils.DEFAULT_RETRY_TEMPLATE;

/**
 * AI Model 模型工厂的实现类
 */
public class AiModelFactoryImpl implements AiModelFactory {

    @Override
    public ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url) {
        String cacheKey = buildClientCacheKey(ChatModel.class, platform, apiKey, url);
        return Singleton.get(cacheKey, (Func0<ChatModel>) () -> {
            // noinspection EnhancedSwitchMigration
            switch (platform) {
                case TONG_YI:
                    return buildTongYiChatModel(apiKey);
                case YI_YAN:
                    return buildYiYanChatModel(apiKey);
                case DEEP_SEEK:
                    return buildDeepSeekChatModel(apiKey);
                case DOU_BAO:
                    return buildDouBaoChatModel(apiKey);
                case HUN_YUAN:
                    return buildHunYuanChatModel(apiKey, url);
                case SILICON_FLOW:
                    return buildSiliconFlowChatModel(apiKey);
                case ZHI_PU:
                    return buildZhiPuChatModel(apiKey, url);
                case MINI_MAX:
                    return buildMiniMaxChatModel(apiKey, url);
                case MOONSHOT:
                    return buildMoonshotChatModel(apiKey, url);
                case XING_HUO:
                    return buildXingHuoChatModel(apiKey);
                case BAI_CHUAN:
                    return buildBaiChuanChatModel(apiKey);
                case OPENAI:
                    return buildOpenAiChatModel(apiKey, url);
                case AZURE_OPENAI:
                    return buildAzureOpenAiChatModel(apiKey, url);
                case ANTHROPIC:
                    return buildAnthropicChatModel(apiKey, url);
                case GEMINI:
                    return buildGeminiChatModel(apiKey);
                case OLLAMA:
                    return buildOllamaChatModel(url);
                case GROK:
                    return buildGrokChatModel(apiKey, url);
                default:
                    throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
            }
        });
    }

    @Override
    public ChatModel getDefaultChatModel(AiPlatformEnum platform) {
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return SpringUtils.getBean(DashScopeChatModel.class);
            case YI_YAN:
                return SpringUtils.getBean(QianFanChatModel.class);
            case DEEP_SEEK:
                return SpringUtils.getBean(DeepSeekChatModel.class);
            case DOU_BAO:
                return SpringUtils.getBean(DouBaoChatModel.class);
            case HUN_YUAN:
                return SpringUtils.getBean(HunYuanChatModel.class);
            case SILICON_FLOW:
                return SpringUtils.getBean(SiliconFlowChatModel.class);
            case ZHI_PU:
                return SpringUtils.getBean(ZhiPuAiChatModel.class);
            case MINI_MAX:
                return SpringUtils.getBean(MiniMaxChatModel.class);
            case MOONSHOT:
                return SpringUtils.getBean(MoonshotChatModel.class);
            case XING_HUO:
                return SpringUtils.getBean(XingHuoChatModel.class);
            case BAI_CHUAN:
                return SpringUtils.getBean(BaiChuanChatModel.class);
            case OPENAI:
                return SpringUtils.getBean(OpenAiChatModel.class);
            case AZURE_OPENAI:
                return SpringUtils.getBean(AzureOpenAiChatModel.class);
            case ANTHROPIC:
                return SpringUtils.getBean(AnthropicChatModel.class);
            case GEMINI:
                return SpringUtils.getBean(GeminiChatModel.class);
            case OLLAMA:
                return SpringUtils.getBean(OllamaChatModel.class);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    @SuppressWarnings("EnhancedSwitchMigration")
    public EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model) {
        String cacheKey = buildClientCacheKey(EmbeddingModel.class, platform, apiKey, url, model);
        return Singleton.get(cacheKey, (Func0<EmbeddingModel>) () -> {
            switch (platform) {
                case TONG_YI:
                    return buildTongYiEmbeddingModel(apiKey, model);
                case YI_YAN:
                    return buildYiYanEmbeddingModel(apiKey, model);
                case ZHI_PU:
                    return buildZhiPuEmbeddingModel(apiKey, url, model);
                case MINI_MAX:
                    return buildMiniMaxEmbeddingModel(apiKey, url, model);
                case OPENAI:
                    return buildOpenAiEmbeddingModel(apiKey, url, model);
                case AZURE_OPENAI:
                    return buildAzureOpenAiEmbeddingModel(apiKey, url, model);
                case OLLAMA:
                    return buildOllamaEmbeddingModel(url, model);
                default:
                    throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
            }
        });
    }

    @Override
    public VectorStore getOrCreateVectorStore(Class<? extends VectorStore> type,
                                              EmbeddingModel embeddingModel,
                                              Map<String, Class<?>> metadataFields) {
        String cacheKey = buildClientCacheKey(VectorStore.class, embeddingModel, type);
        return Singleton.get(cacheKey, (Func0<VectorStore>) () -> {
            if (type == SimpleVectorStore.class) {
                return buildSimpleVectorStore(embeddingModel);
            }
            if (type == QdrantVectorStore.class) {
                return buildQdrantVectorStore(embeddingModel);
            }
            if (type == RedisVectorStore.class) {
                return buildRedisVectorStore(embeddingModel, metadataFields);
            }
            throw new IllegalArgumentException(StrUtil.format("未知类型({})", type));
        });
    }

    private static String buildClientCacheKey(Class<?> clazz, Object... params) {
        if (ArrayUtil.isEmpty(params)) {
            return clazz.getName();
        }
        return StrUtil.format("{}#{}", clazz.getName(), ArrayUtil.join(params, "_"));
    }

    // ========== 各种创建 ChatModel 的方法 ==========

    private static DashScopeChatModel buildTongYiChatModel(String key) {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(key).build();
        DashScopeChatOptions options = DashScopeChatOptions.builder().withModel(DashScopeApi.DEFAULT_CHAT_MODEL)
                .withTemperature(0.7).build();
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .toolCallingManager(getToolCallingManager())
                .build();
    }

    private static QianFanChatModel buildYiYanChatModel(String key) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys.get(0);
        String secretKey = keys.get(1);
        QianFanApi qianFanApi = new QianFanApi(appKey, secretKey);
        return new QianFanChatModel(qianFanApi);
    }

    private static DeepSeekChatModel buildDeepSeekChatModel(String apiKey) {
        DeepSeekApi deepSeekApi = DeepSeekApi.builder().apiKey(apiKey).build();
        DeepSeekChatOptions options = DeepSeekChatOptions.builder().model(DeepSeekApi.DEFAULT_CHAT_MODEL)
                .temperature(0.7).build();
        return DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi)
                .defaultOptions(options)
                .toolCallingManager(getToolCallingManager())
                .build();
    }

    private ChatModel buildDouBaoChatModel(String apiKey) {
        AiProperties.DouBao properties = new AiProperties.DouBao();
        properties.setApiKey(apiKey);
        return new AiAutoConfiguration().buildDouBaoChatClient(properties);
    }

    private ChatModel buildHunYuanChatModel(String apiKey, String url) {
        AiProperties.HunYuan properties = new AiProperties.HunYuan();
        properties.setBaseUrl(url);
        properties.setApiKey(apiKey);
        return new AiAutoConfiguration().buildHunYuanChatClient(properties);
    }

    private ChatModel buildSiliconFlowChatModel(String apiKey) {
        AiProperties.SiliconFlow properties = new AiProperties.SiliconFlow();
        properties.setApiKey(apiKey);
        return new AiAutoConfiguration().buildSiliconFlowChatClient(properties);
    }

    private ZhiPuAiChatModel buildZhiPuChatModel(String apiKey, String url) {
        ZhiPuAiApi.Builder zhiPuAiApiBuilder = ZhiPuAiApi.builder().apiKey(apiKey);
        if (StrUtil.isNotEmpty(url)) {
            zhiPuAiApiBuilder.baseUrl(url);
        }
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder().model(ZhiPuAiApi.DEFAULT_CHAT_MODEL).temperature(0.7).build();
        return new ZhiPuAiChatModel(zhiPuAiApiBuilder.build(), options, getToolCallingManager(), DEFAULT_RETRY_TEMPLATE,
                getObservationRegistry().getIfAvailable());
    }

    private MiniMaxChatModel buildMiniMaxChatModel(String apiKey, String url) {
        MiniMaxApi miniMaxApi = StrUtil.isEmpty(url) ? new MiniMaxApi(apiKey)
                : new MiniMaxApi(url, apiKey);
        MiniMaxChatOptions options = MiniMaxChatOptions.builder().model(MiniMaxApi.DEFAULT_CHAT_MODEL).temperature(0.7).build();
        return new MiniMaxChatModel(miniMaxApi, options, getToolCallingManager(), DEFAULT_RETRY_TEMPLATE);
    }

    private MoonshotChatModel buildMoonshotChatModel(String apiKey, String url) {
        MoonshotApi.Builder moonshotApiBuilder = MoonshotApi.builder()
                .apiKey(apiKey);
        if (StrUtil.isNotEmpty(url)) {
            moonshotApiBuilder.baseUrl(url);
        }
        MoonshotChatOptions options = MoonshotChatOptions.builder().model(MoonshotApi.DEFAULT_CHAT_MODEL).build();
        return MoonshotChatModel.builder()
                .moonshotApi(moonshotApiBuilder.build())
                .defaultOptions(options)
                .toolCallingManager(getToolCallingManager())
                .build();
    }

    private static XingHuoChatModel buildXingHuoChatModel(String key) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 2, "XingHuoChatClient 的密钥需要 (appKey|secretKey) 格式");
        AiProperties.XingHuo properties = new AiProperties.XingHuo();
        properties.setAppKey(keys.get(0));
        properties.setSecretKey(keys.get(1));
        return new AiAutoConfiguration().buildXingHuoChatClient(properties);
    }

    private BaiChuanChatModel buildBaiChuanChatModel(String apiKey) {
        AiProperties.BaiChuan properties = new AiProperties.BaiChuan();
        properties.setApiKey(apiKey);
        return new AiAutoConfiguration().buildBaiChuanChatClient(properties);
    }

    private static OpenAiChatModel buildOpenAiChatModel(String openAiToken, String url) {
        url = StrUtil.blankToDefault(url, OpenAiApiConstants.DEFAULT_BASE_URL);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(url).apiKey(openAiToken).build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .toolCallingManager(getToolCallingManager())
                .build();
    }

    private static AzureOpenAiChatModel buildAzureOpenAiChatModel(String apiKey, String url) {
        OpenAIClientBuilder openAIClientBuilder = new OpenAIClientBuilder()
                .endpoint(url).credential(new KeyCredential(apiKey));
        return AzureOpenAiChatModel.builder()
                .openAIClientBuilder(openAIClientBuilder)
                .toolCallingManager(getToolCallingManager())
                .build();
    }

    private static AnthropicChatModel buildAnthropicChatModel(String apiKey, String url) {
        AnthropicApi.Builder builder = AnthropicApi.builder().apiKey(apiKey);
        if (StrUtil.isNotEmpty(url)) {
            builder.baseUrl(url);
        }
        AnthropicApi anthropicApi = builder.build();
        return AnthropicChatModel.builder()
                .anthropicApi(anthropicApi)
                .toolCallingManager(getToolCallingManager())
                .build();
    }

    private static GeminiChatModel buildGeminiChatModel(String apiKey) {
        AiProperties.Gemini properties = SpringUtils.getBean(AiProperties.class)
                .getGemini();
        properties.setApiKey(apiKey);
        return new AiAutoConfiguration().buildGeminiChatClient(properties);
    }

    private static OllamaChatModel buildOllamaChatModel(String url) {
        OllamaApi ollamaApi = OllamaApi.builder().baseUrl(url).build();
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .toolCallingManager(getToolCallingManager())
                .build();
    }

    private ChatModel buildGrokChatModel(String apiKey, String url) {
        AiProperties.Grok properties = new AiProperties.Grok();
        properties.setBaseUrl(url);
        properties.setApiKey(apiKey);
        return new AiAutoConfiguration().buildGrokChatClient(properties);
    }

    // ========== 各种创建 EmbeddingModel 的方法 ==========

    private DashScopeEmbeddingModel buildTongYiEmbeddingModel(String apiKey, String model) {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(apiKey).build();
        DashScopeEmbeddingOptions dashScopeEmbeddingOptions = DashScopeEmbeddingOptions.builder().withModel(model).build();
        return new DashScopeEmbeddingModel(dashScopeApi, MetadataMode.EMBED, dashScopeEmbeddingOptions);
    }

    private ZhiPuAiEmbeddingModel buildZhiPuEmbeddingModel(String apiKey, String url, String model) {
        ZhiPuAiApi.Builder zhiPuAiApiBuilder = ZhiPuAiApi.builder().apiKey(apiKey);
        if (StrUtil.isNotEmpty(url)) {
            zhiPuAiApiBuilder.baseUrl(url);
        }
        ZhiPuAiEmbeddingOptions zhiPuAiEmbeddingOptions = ZhiPuAiEmbeddingOptions.builder().model(model).build();
        return new ZhiPuAiEmbeddingModel(zhiPuAiApiBuilder.build(), MetadataMode.EMBED, zhiPuAiEmbeddingOptions);
    }

    private EmbeddingModel buildMiniMaxEmbeddingModel(String apiKey, String url, String model) {
        MiniMaxApi miniMaxApi = StrUtil.isEmpty(url) ? new MiniMaxApi(apiKey)
                : new MiniMaxApi(url, apiKey);
        MiniMaxEmbeddingOptions miniMaxEmbeddingOptions = MiniMaxEmbeddingOptions.builder().model(model).build();
        return new MiniMaxEmbeddingModel(miniMaxApi, MetadataMode.EMBED, miniMaxEmbeddingOptions);
    }

    private QianFanEmbeddingModel buildYiYanEmbeddingModel(String key, String model) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys.get(0);
        String secretKey = keys.get(1);
        QianFanApi qianFanApi = new QianFanApi(appKey, secretKey);
        QianFanEmbeddingOptions qianFanEmbeddingOptions = QianFanEmbeddingOptions.builder().model(model).build();
        return new QianFanEmbeddingModel(qianFanApi, MetadataMode.EMBED, qianFanEmbeddingOptions);
    }

    private OllamaEmbeddingModel buildOllamaEmbeddingModel(String url, String model) {
        OllamaApi ollamaApi = OllamaApi.builder().baseUrl(url).build();
        OllamaEmbeddingOptions ollamaOptions = OllamaEmbeddingOptions.builder().model(model).build();
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(ollamaOptions)
                .build();
    }

    private OpenAiEmbeddingModel buildOpenAiEmbeddingModel(String openAiToken, String url, String model) {
        url = StrUtil.blankToDefault(url, OpenAiApiConstants.DEFAULT_BASE_URL);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(url).apiKey(openAiToken).build();
        OpenAiEmbeddingOptions openAiEmbeddingProperties = OpenAiEmbeddingOptions.builder().model(model).build();
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, openAiEmbeddingProperties);
    }

    private AzureOpenAiEmbeddingModel buildAzureOpenAiEmbeddingModel(String apiKey, String url, String model) {
        AzureOpenAiEmbeddingAutoConfiguration azureOpenAiAutoConfiguration = new AzureOpenAiEmbeddingAutoConfiguration();
        OpenAIClientBuilder openAIClientBuilder = new OpenAIClientBuilder()
                .endpoint(url).credential(new KeyCredential(apiKey));
        AzureOpenAiEmbeddingProperties embeddingProperties = SpringUtils.getBean(AzureOpenAiEmbeddingProperties.class);
        return azureOpenAiAutoConfiguration.azureOpenAiEmbeddingModel(openAIClientBuilder, embeddingProperties,
                getObservationRegistry(), getEmbeddingModelObservationConvention());
    }

    // ========== 各种创建 VectorStore 的方法 ==========

    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private SimpleVectorStore buildSimpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(StrUtil.format("{}/vector_store/simple_{}.json",
                FileUtil.getUserHomePath(), embeddingModel.getClass().getSimpleName()));
        if (!file.exists()) {
            FileUtil.mkParentDirs(file);
            file.createNewFile();
        } else if (file.length() > 0) {
            vectorStore.load(file);
        }
        Timer timer = new Timer("SimpleVectorStoreTimer-" + file.getAbsolutePath());
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                vectorStore.save(file);
            }
        }, Duration.ofMinutes(1).toMillis(), Duration.ofMinutes(1).toMillis());
        RuntimeUtil.addShutdownHook(() -> vectorStore.save(file));
        return vectorStore;
    }

    @SneakyThrows
    private QdrantVectorStore buildQdrantVectorStore(EmbeddingModel embeddingModel) {
        QdrantVectorStoreAutoConfiguration configuration = new QdrantVectorStoreAutoConfiguration();
        QdrantVectorStoreProperties properties = SpringUtils.getBean(QdrantVectorStoreProperties.class);
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                properties.getHost(), properties.getPort(), properties.isUseTls());
        if (StrUtil.isNotEmpty(properties.getApiKey())) {
            grpcClientBuilder.withApiKey(properties.getApiKey());
        }
        QdrantClient qdrantClient = new QdrantClient(grpcClientBuilder.build());
        QdrantVectorStore vectorStore = configuration.vectorStore(embeddingModel, properties, qdrantClient,
                getObservationRegistry(), getCustomObservationConvention(), getBatchingStrategy());
        vectorStore.afterPropertiesSet();
        return vectorStore;
    }

    private RedisVectorStore buildRedisVectorStore(EmbeddingModel embeddingModel,
                                                   Map<String, Class<?>> metadataFields) {
        RedisProperties redisProperties = SpringUtils.getBean(RedisProperties.class);
        JedisPooled jedisPooled = new JedisPooled(redisProperties.getHost(), redisProperties.getPort(),
                redisProperties.getUsername(), redisProperties.getPassword());
        RedisVectorStoreProperties properties = SpringUtils.getBean(RedisVectorStoreProperties.class);
        RedisVectorStore redisVectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(properties.getIndexName()).prefix(properties.getPrefix())
                .initializeSchema(properties.isInitializeSchema())
                .metadataFields(convertList(metadataFields.entrySet(), entry -> {
                    String fieldName = entry.getKey();
                    Class<?> fieldType = entry.getValue();
                    if (Number.class.isAssignableFrom(fieldType)) {
                        return RedisVectorStore.MetadataField.numeric(fieldName);
                    }
                    if (Boolean.class.isAssignableFrom(fieldType)) {
                        return RedisVectorStore.MetadataField.tag(fieldName);
                    }
                    return RedisVectorStore.MetadataField.text(fieldName);
                }))
                .observationRegistry(getObservationRegistry().getObject())
                .customObservationConvention(getCustomObservationConvention().getObject())
                .batchingStrategy(getBatchingStrategy())
                .build();
        redisVectorStore.afterPropertiesSet();
        return redisVectorStore;
    }

    private static ObjectProvider<ObservationRegistry> getObservationRegistry() {
        return new ObjectProvider<>() {
            @Override
            public ObservationRegistry getObject() throws BeansException {
                return SpringUtils.getBean(ObservationRegistry.class);
            }
        };
    }

    private static ObjectProvider<VectorStoreObservationConvention> getCustomObservationConvention() {
        return new ObjectProvider<>() {
            @Override
            public VectorStoreObservationConvention getObject() throws BeansException {
                return new DefaultVectorStoreObservationConvention();
            }
        };
    }

    private static BatchingStrategy getBatchingStrategy() {
        return SpringUtils.getBean(BatchingStrategy.class);
    }

    private static ToolCallingManager getToolCallingManager() {
        return SpringUtils.getBean(ToolCallingManager.class);
    }

    private static ObjectProvider<EmbeddingModelObservationConvention> getEmbeddingModelObservationConvention() {
        return new ObjectProvider<>() {
            @Override
            public EmbeddingModelObservationConvention getObject() throws BeansException {
                return SpringUtils.getBean(EmbeddingModelObservationConvention.class);
            }
        };
    }

}
