package com.ssafy.yamyam_coach.repository.mealfood;

import com.ssafy.yamyam_coach.domain.mealfood.MealFood;

import java.util.List;
import java.util.Optional;

public interface MealFoodRepository {

    int insert(MealFood mealFood);

    Optional<MealFood> findById(Long mealFoodId);

    int batchInsert(List<MealFood> mealFoods);
}
