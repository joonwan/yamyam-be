package com.ssafy.yamyam_coach.service.daily_diet.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Data
public class DailyDietResponse {

    private Long dailyDietId;
    private LocalDate date;
    private String dayOfWeek;
    private String description;

    @Builder
    private DailyDietResponse(Long dailyDietId, LocalDate date, String dayOfWeek, String description) {
        this.dailyDietId = dailyDietId;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.description = description;
    }
}
