package com.ssafy.yamyam_coach.domain.daily_diet;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyDiet {

    private Long id;
    private Long dietPlanId;
    private LocalDate date;
    private String description;

    @Builder
    private DailyDiet(Long dietPlanId, LocalDate date, String description) {
        this.dietPlanId = dietPlanId;
        this.date = date;
        this.description = description;
    }
}
