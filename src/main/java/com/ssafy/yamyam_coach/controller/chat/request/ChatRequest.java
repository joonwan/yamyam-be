package com.ssafy.yamyam_coach.controller.chat.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChatRequest {

    private Long dailyDietId;
    private Long bodySpecId;

    @NotNull(message = "content 는 필수 입니다.")
    private String content;
}
