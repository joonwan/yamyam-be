package com.ssafy.yamyam_coach.service.diet_plan.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DietPlanServiceResponse {

    private Long dietPlanId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    @Builder
    private DietPlanServiceResponse(Long dietPlanId, String title, LocalDate startDate, LocalDate endDate) {
        this.dietPlanId = dietPlanId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
