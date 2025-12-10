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
    private Long id;                    // daily detail id
    private Long dietPlanId;            // diet plan id
    private LocalDate date;             // 날짜
    private String description;         // 설명
    private List<MealDetail> meals;     // 해당일에 있는 아침, 점심, 저녁, 간식 세부 정보
}
