package com.ssafy.yamyam_coach.service.food.response;

import lombok.Builder;
import lombok.Data;

@Data
public class SearchFoodServiceResponse {

    private String name;
    private String category;
    private Double caloriePerG;
    private Double caloriePerMl;

    @Builder
    private SearchFoodServiceResponse(String name, String category, Double caloriePerG, Double caloriePerMl) {
        this.name = name;
        this.category = category;
        this.caloriePerG = caloriePerG;
        this.caloriePerMl = caloriePerMl;
    }
}
