package com.ssafy.yamyam_coach.service.challenge.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChallengeDetailResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private int progress; // 계산된 진행률 (%)
    private List<String> successDates; // 내가 성공 체크한 날짜들 (예: ["2025-01-01", "2025-01-02"])
}