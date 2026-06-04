package cn.boss.data.ai.framework.ai.config;

import cn.boss.data.ai.framework.common.util.spring.SpringUtils;
import cn.boss.data.ai.framework.ai.core.model.AiModelFactory;
import cn.boss.data.ai.framework.ai.core.model.AiModelFactoryImpl;
import cn.boss.data.ai.framework.ai.core.websearch.AiWebSearchClient;
import cn.boss.data.ai.framework.ai.core.websearch.bocha.AiBoChaWebSearchClient;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
