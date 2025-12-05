package com.ssafy.yamyam_coach.domain.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {

    private Long id;              // PK, Auto Increment

    private String title;         // 챌린지 제목
    private String description;   // 챌린지 설명 (TEXT 타입도 String으로 받음)

    private LocalDateTime startDate; // 시작일 (start_date -> startDate)
    private LocalDateTime endDate;   // 종료일 (end_date -> endDate)

    private LocalDateTime createdAt; // 생성일

    // ID 제외, 챌린지 생성 시 필요한 정보만 담는 빌더
    @Builder
    private Challenge(String title, String description, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime createdAt) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
    }
}