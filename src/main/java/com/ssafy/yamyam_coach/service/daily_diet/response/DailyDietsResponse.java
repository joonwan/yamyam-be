package com.ssafy.yamyam_coach.service.daily_diet.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class DailyDietsResponse {
    List<DailyDietResponse> dailyDiets;

    @Builder
    private DailyDietsResponse(List<DailyDietResponse> dailyDiets) {
        this.dailyDiets = dailyDiets;
    }
}
