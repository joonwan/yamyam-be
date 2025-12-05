package com.ssafy.yamyam_coach.domain.challenge_participation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeParticipation {

    private Long id;            // PK
    private Long userId;        // FK (참여자 ID)
    private Long challengeId;   // FK (참여한 챌린지 ID)
    private LocalDateTime created_at; // 참가일 (DATETIME)
    
    private String status;      // 참여 상태 (예: "PROGRESS", "COMPLETED", "STOPPED")

    // ID 제외, 참여 정보 생성용 빌더
    @Builder
    public ChallengeParticipation(Long userId, Long challengeId, String status) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.status = status;
    }
}