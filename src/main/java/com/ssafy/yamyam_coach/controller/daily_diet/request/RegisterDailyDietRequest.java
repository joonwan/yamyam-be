package com.ssafy.yamyam_coach.controller.daily_diet.request;

import com.ssafy.yamyam_coach.service.daily_diet.request.RegisterDailyDietServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterDailyDietRequest {

    @NotNull(message = "식단 계획 id 는 필수입니다.")
    private Long dietPlanId;

    @NotNull(message = "date 값은 필수 입니다.")
    private LocalDate date;

    @NotBlank(message = "description 을 반드시 입력해 주세요")
    private String description;

    public RegisterDailyDietServiceRequest toServiceRequest() {
        return RegisterDailyDietServiceRequest.builder()
                .dietPlanId(dietPlanId)
                .date(date)
                .description(description)
                .build();
    }

    @Builder
    private RegisterDailyDietRequest(Long dietPlanId, LocalDate date, String description) {
        this.dietPlanId = dietPlanId;
        this.date = date;
        this.description = description;
    }
}
