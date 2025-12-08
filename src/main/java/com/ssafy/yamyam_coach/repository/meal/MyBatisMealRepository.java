package com.ssafy.yamyam_coach.repository.meal;

import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.mapper.meal.MealMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MyBatisMealRepository implements MealRepository {

    private final MealMapper mealMapper;

    @Override
    public int insert(Meal meal) {
        return mealMapper.insert(meal);
    }

    @Override
    public List<Meal> findByDailyDietId(Long dailyDietId) {
        return mealMapper.findByDailyDietId(dailyDietId);
    }
}
