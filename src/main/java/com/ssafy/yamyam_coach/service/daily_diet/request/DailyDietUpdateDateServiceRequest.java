package com.ssafy.yamyam_coach.service.daily_diet.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyDietUpdateDateServiceRequest {

    private Long dailyDietId;
    private LocalDate newDate;

    @Builder
    private DailyDietUpdateDateServiceRequest(Long dailyDietId, LocalDate newDate) {
        this.dailyDietId = dailyDietId;
        this.newDate = newDate;
    }
}
