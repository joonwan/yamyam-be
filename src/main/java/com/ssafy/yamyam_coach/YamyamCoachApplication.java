package com.ssafy.yamyam_coach;

import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiChatAutoConfiguration;
import org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiEmbeddingConnectionAutoConfiguration;
import org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingAutoConfiguration;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        RedisVectorStoreAutoConfiguration.class,
        GoogleGenAiChatAutoConfiguration.class
})
public class YamyamCoachApplication {

	public static void main(String[] args) {
		SpringApplication.run(YamyamCoachApplication.class, args);
	}
}
