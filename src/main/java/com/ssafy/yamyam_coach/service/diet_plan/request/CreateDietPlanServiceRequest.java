package com.ssafy.yamyam_coach.service.diet_plan.request;

import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateMealRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateDietPlanServiceRequest {

    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;

    @Builder
    private CreateDietPlanServiceRequest(String title, String content, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
