package com.ssafy.yamyam_coach.service.daily_diet.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyDietUpdateDescriptionServiceRequest {

    private Long dailyDietId;
    private LocalDate date;
    private String newDescription;

    @Builder
    private DailyDietUpdateDescriptionServiceRequest(Long dailyDietId, LocalDate date, String newDescription) {
        this.dailyDietId = dailyDietId;
        this.date = date;
        this.newDescription = newDescription;
    }
}
