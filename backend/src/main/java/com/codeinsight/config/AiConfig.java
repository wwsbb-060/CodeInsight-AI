package com.codeinsight.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 配置中心。
 * 切模型只需改 yml 里的 ai.provider，其余代码不感知。
 */
@Configuration
public class AiConfig {

    @Value("${ai.provider}")
    private String provider;

    @Value("${ai.deepseek.api-key}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.model}")
    private String deepseekModel;

    @Value("${ai.deepseek.base-url}")
    private String deepseekBaseUrl;

    @Value("${ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${ai.openai.model}")
    private String openaiModel;

    @Value("${ai.embedding.model}")
    private String embeddingModel;

    /**
     * 当前激活的 LLM 客户端
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if ("openai".equalsIgnoreCase(provider)) {
            return OpenAiChatModel.builder()
                    .apiKey(openaiApiKey)
                    .modelName(openaiModel)
                    .timeout(Duration.ofSeconds(120))
                    .build();
        }
        return OpenAiChatModel.builder()
                .baseUrl(deepseekBaseUrl)
                .apiKey(deepseekApiKey)
                .modelName(deepseekModel)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * Embedding 模型，用于代码文本向量化。
     * 使用 DeepSeek 兼容的 OpenAI Embedding 接口。
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(deepseekBaseUrl)
                .apiKey(deepseekApiKey)
                .modelName(embeddingModel)
                .build();
    }

    /**
     * 当前激活的模型名称，与 chatLanguageModel 保持一致。
     * 业务代码注入它，不关心底层是 deepseek 还是 openai。
     */
    @Bean
    public String currentModelName() {
        if ("openai".equalsIgnoreCase(provider)) {
            return openaiModel;
        }
        return deepseekModel;
    }
}
