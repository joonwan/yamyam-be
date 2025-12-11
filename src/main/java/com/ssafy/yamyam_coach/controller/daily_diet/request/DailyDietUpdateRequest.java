package com.ssafy.yamyam_coach.controller.daily_diet.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyDietUpdateDescriptionRequest {

    private String newDescription;
    private LocalDate newDate;

}
