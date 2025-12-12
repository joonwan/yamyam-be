package com.ssafy.yamyam_coach.repository.meal;

import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.repository.meal.response.MealDetail;

import java.util.List;
import java.util.Optional;

public interface MealRepository {

    int insert(Meal meal);

    Optional<Meal> findById(Long mealId);

    Optional<MealDetail> findMealDetailById(Long mealId);

    int deleteByDailyDietId(Long dailyDietId);

    boolean existsByDailyDietAndMealType(Long dailyDietId, MealType mealType);

    Optional<Meal> findByDailyDietAndMealType(Long dailyDietId, MealType mealType);

    int deleteById(Long mealId);

    int updateMealType(Long mealId, MealType mealType);
}
