package com.ssafy.yamyam_coach.repository.daily_diet.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyDietDetail {
    private Long id;
    private Long dietPlanId;
    private LocalDate date;
    private String description;
    private List<MealDetail> meals;
}
