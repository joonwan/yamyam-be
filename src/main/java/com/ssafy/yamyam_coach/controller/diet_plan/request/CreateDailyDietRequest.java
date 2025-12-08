package com.ssafy.yamyam_coach.controller.diet_plan.request;

import com.ssafy.yamyam_coach.service.daily_diet.request.CreateDailyDietServiceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateDailyDietRequest {

    @NotNull(message = "날짜 값은 필수 입니다.")
    private LocalDate date;

    @NotBlank(message = "설명은 필수 입니다.")
    private String description;

    @Valid
    @NotNull(message = "아침 식단은 null 이 될 수 없습니다.")
    private List<CreateMealFoodRequest> breakfast;

    @Valid
    @NotNull(message = "점심 식단은 null 이 될 수 없습니다.")
    private List<CreateMealFoodRequest> lunch;

    @Valid
    @NotNull(message = "저녁 식단은 null 이 될 수 없습니다.")
    private List<CreateMealFoodRequest> dinner;

    @Valid
    @NotNull(message = "간식 식단은 null 이 될 수 없습니다.")
    private List<CreateMealFoodRequest> snack;

    public CreateDailyDietServiceRequest toServiceRequest() {
        return CreateDailyDietServiceRequest.builder()
                .date(date)
                .description(description)
                .breakfast(breakfast.stream().map(CreateMealFoodRequest::toServiceRequest).toList())
                .lunch(lunch.stream().map(CreateMealFoodRequest::toServiceRequest).toList())
                .dinner(dinner.stream().map(CreateMealFoodRequest::toServiceRequest).toList())
                .snack(snack.stream().map(CreateMealFoodRequest::toServiceRequest).toList())
                .build();
    }

}
