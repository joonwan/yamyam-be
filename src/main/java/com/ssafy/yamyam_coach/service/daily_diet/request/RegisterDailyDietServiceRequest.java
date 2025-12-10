package com.ssafy.yamyam_coach.service.daily_diet.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterDailyDietServiceRequest {

    private Long dietPlanId;

    private LocalDate date;

    private String description;

    @Builder
    private RegisterDailyDietServiceRequest(Long dietPlanId, LocalDate date, String description) {
        this.dietPlanId = dietPlanId;
        this.date = date;
        this.description = description;
    }
}
