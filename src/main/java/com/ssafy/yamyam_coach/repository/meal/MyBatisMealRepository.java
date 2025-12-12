package com.ssafy.yamyam_coach.repository.meal;

import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.mapper.meal.MealMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisMealRepository implements MealRepository {

    private final MealMapper mealMapper;

    @Override
    public int insert(Meal meal) {
        return mealMapper.insert(meal);
    }

    @Override
    public Optional<Meal> findById(Long mealId) {
        return Optional.ofNullable(mealMapper.findById(mealId));
    }

    @Override
    public int deleteByDailyDietId(Long dailyDietId) {
        return mealMapper.deleteByDailyDietId(dailyDietId);
    }

    @Override
    public boolean existsByDailyDietAndMealType(Long dailyDietId, MealType mealType) {
        return mealMapper.existsByDailyDietAndMealType(dailyDietId, mealType);
    }

    @Override
    public Optional<Meal> findByDailyDietAndMealType(Long dailyDietId, MealType mealType) {
        return Optional.ofNullable(mealMapper.findByDailyDietAndMealType(dailyDietId, mealType));
    }

    @Override
    public int deleteById(Long mealId) {
        return mealMapper.deleteById(mealId);
    }
}
