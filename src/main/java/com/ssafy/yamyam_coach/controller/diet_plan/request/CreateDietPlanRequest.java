package com.ssafy.yamyam_coach.controller.diet_plan.request;

import com.ssafy.yamyam_coach.service.diet_plan.request.CreateDietPlanServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDietPlanRequest {

    @NotBlank(message = "제목을 반드시 입력해 주세요.")
    private String title;

    @NotBlank(message = "내용을 반드시 입력해주세요.")
    private String content;

    @NotNull(message = "시작일을 반드시 입력해주세요.")
    private LocalDate startDate;

    @NotNull(message = "종료일을 반드시 입력해주세요.")
    private LocalDate endDate;

    public CreateDietPlanServiceRequest toServiceRequest() {
        return CreateDietPlanServiceRequest.builder()
                .title(title)
                .content(content)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
