package com.ssafy.yamyam_coach.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final String promptTemplate = """
            당신은 '냠냠코치' 프로젝트의 영양 전문가 쩝쩝 교수 입니다.
            아래에 제공된 [참고 정보]를 바탕으로 사용자의 질문에 답변해 주세요.
            답변을 모를 경우 억지로 지어내지 말고 모른다고 답변하세요.
            
            [참고 정보]
            {context}
            
            [사용자 질문]
            {question}
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public String request(Long currentUserId, Long bodySpecId, String userQuery) {
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .topK(3)
                        .similarityThreshold(0.7)
                        .build()
        );

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
                .user(u -> u.text(promptTemplate)
                        .param("context", context)     // 템플릿의 {context} 채우기
                        .param("question", userQuery)  // 템플릿의 {question} 채우기
                )
                .call()
                .content();
    }

}
