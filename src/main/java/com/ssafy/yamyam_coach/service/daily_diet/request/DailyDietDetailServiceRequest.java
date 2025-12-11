package com.ssafy.yamyam_coach.service.daily_diet.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyDietDetailServiceRequest {

    private Long dietPlanId;
    private LocalDate date;

    @Builder
    private DailyDietDetailServiceRequest(Long dietPlanId, LocalDate date) {
        this.dietPlanId = dietPlanId;
        this.date = date;
    }
}
