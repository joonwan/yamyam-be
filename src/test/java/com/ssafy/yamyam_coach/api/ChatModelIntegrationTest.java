package com.ssafy.yamyam_coach.api;

import com.ssafy.yamyam_coach.IntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ChatModelIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ChatModel chatModel;

    @Test
    void realApiTest() {
        String response = chatModel.call("안녕하세요");
        System.out.println("AI 응답: " + response);
        assertThat(response).isNotEmpty();
    }
}
