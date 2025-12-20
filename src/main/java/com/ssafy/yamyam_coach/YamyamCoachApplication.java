package com.ssafy.yamyam_coach;

import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = RedisVectorStoreAutoConfiguration.class)
public class YamyamCoachApplication {

	public static void main(String[] args) {
		SpringApplication.run(YamyamCoachApplication.class, args);
	}

}
