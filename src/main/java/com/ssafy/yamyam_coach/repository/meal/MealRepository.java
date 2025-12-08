package com.ssafy.yamyam_coach.repository.meal;

import com.ssafy.yamyam_coach.domain.meals.Meal;

import java.util.List;

public interface MealRepository {

    int insert(Meal meal);

    List<Meal> findByDailyDietId(Long id);
}
