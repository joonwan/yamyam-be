package com.ssafy.yamyam_coach.mapper.meal;

import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MealMapper {

    int insert(Meal meal);

    int deleteByDailyDietId(Long dailyDietId);

    Meal findById(Long mealId);

    boolean existsByDailyDietAndMealType(@Param("dailyDietId") Long dailyDietId, @Param("mealType") MealType mealType);

    Meal findByDailyDietAndMealType(Long dailyDietId, MealType mealType);

    int deleteById(Long mealId);

    int updateMealType(Long mealId, MealType mealType);
}
