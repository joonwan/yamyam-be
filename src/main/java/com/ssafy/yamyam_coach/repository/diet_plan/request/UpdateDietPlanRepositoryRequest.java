package com.ssafy.yamyam_coach.repository.diet_plan.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateDietPlanRepositoryRequest {

    private Long dietPlanId;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;

}
