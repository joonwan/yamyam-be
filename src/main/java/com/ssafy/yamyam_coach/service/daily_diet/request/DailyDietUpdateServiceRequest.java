package com.ssafy.yamyam_coach.service.daily_diet.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyDietUpdateServiceRequest {

    private Long dailyDietId;
    private LocalDate date;
    private String description;

    @Builder
    private DailyDietUpdateServiceRequest(Long dailyDietId, LocalDate date, String description) {
        this.dailyDietId = dailyDietId;
        this.date = date;
        this.description = description;
    }
}
