package com.ssafy.yamyam_coach.service.food.response;

import lombok.Builder;
import lombok.Data;

@Data
public class SearchFoodServiceResponse {

    private Long foodId;
    private String name;
    private String category;
    private Double caloriePerG;
    private Double caloriePerMl;

    @Builder
    private SearchFoodServiceResponse(Long foodId, String name, String category, Double caloriePerG, Double caloriePerMl) {
        this.foodId = foodId;
        this.name = name;
        this.category = category;
        this.caloriePerG = caloriePerG;
        this.caloriePerMl = caloriePerMl;
    }
}
