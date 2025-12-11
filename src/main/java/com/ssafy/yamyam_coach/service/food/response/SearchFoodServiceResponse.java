package com.ssafy.yamyam_coach.service.food.response;

import com.ssafy.yamyam_coach.domain.food.BaseUnit;
import lombok.Builder;
import lombok.Data;

@Data
public class SearchFoodServiceResponse {

    private Long foodId;
    private String name;
    private String category;
    private Double caloriePer100;
    private BaseUnit baseUnit;

    @Builder
    private SearchFoodServiceResponse(Long foodId, String name, String category, Double caloriePer100, BaseUnit baseUnit) {
        this.foodId = foodId;
        this.name = name;
        this.category = category;
        this.caloriePer100 = caloriePer100;
        this.baseUnit = baseUnit;
    }
}
