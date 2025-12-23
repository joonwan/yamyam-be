package com.ssafy.yamyam_coach.controller.chat.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class ChatRequest {

    private List<Long> bodySpecIds;   // 선택한 신체 정보 ID 목록
    private List<Long> dailyDietIds;  // 선택한 일일 식단 ID 목록
    private List<Long> challengeIds;  // 선택한 챌린지 ID 목록

    @NotNull(message = "content 는 필수 입니다.")
    private String content;
}
