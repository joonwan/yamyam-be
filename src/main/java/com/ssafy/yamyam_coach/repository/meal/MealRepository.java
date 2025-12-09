package com.ssafy.yamyam_coach.repository.meal;

import com.ssafy.yamyam_coach.domain.meals.Meal;

import java.util.List;
import java.util.Optional;

public interface MealRepository {

    int insert(Meal meal);

    Optional<Meal> findById(Long mealId);

    int deleteByDailyDietId(Long dailyDietId);
}
