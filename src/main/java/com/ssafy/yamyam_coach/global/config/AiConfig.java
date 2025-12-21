package com.ssafy.yamyam_coach.global.config;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig{

    @Value("${spring.ai.google.genai.api-key}")
    private String genaiApiKey;

    @Value("${spring.ai.google.genai.base-url}")
    private String genaiBaseUrl;

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    }

    @Bean
    ChatModel chatModel() {
        Client client = Client.builder().apiKey(genaiApiKey)
                .httpOptions(HttpOptions.builder().baseUrl(genaiBaseUrl).build())
                .build();

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model("gemini-2.0-flash")
                .temperature(0.7)
                .maxOutputTokens(1000)
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(client)
                .defaultOptions(options)
                .build();
    }

    // OpenAI Embedding은 AutoConfiguration 사용
    // application.yaml에서 설정
}
