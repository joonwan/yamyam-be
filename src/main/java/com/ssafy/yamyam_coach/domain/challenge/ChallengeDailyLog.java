package com.ssafy.yamyam_coach.domain.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDailyLog {
    private Long id;
    private Long userId;
    private Long challengeId;
    private LocalDate logDate;
}