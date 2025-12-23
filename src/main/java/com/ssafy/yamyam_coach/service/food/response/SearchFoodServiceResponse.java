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
    private Double proteinPer100;
    private Double carbohydratePer100;
    private Double fatPer100;
    private Double sugarPer100;
    private Double sodiumPer100;

    @Builder
    private SearchFoodServiceResponse(Long foodId, String name, String category, Double caloriePer100, BaseUnit baseUnit, Double proteinPer100, Double carbohydratePer100, Double fatPer100, Double sugarPer100, Double sodiumPer100) {
        this.foodId = foodId;
        this.name = name;
        this.category = category;
        this.caloriePer100 = caloriePer100;
        this.baseUnit = baseUnit;
        this.proteinPer100 = proteinPer100;
        this.carbohydratePer100 = carbohydratePer100;
        this.fatPer100 = fatPer100;
        this.sugarPer100 = sugarPer100;
        this.sodiumPer100 = sodiumPer100;
    }
}
