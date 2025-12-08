package com.ssafy.yamyam_coach.mapper.meal;

import com.ssafy.yamyam_coach.domain.meals.Meal;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MealMapper {

    int insert(Meal meal);

    List<Meal> findByDailyDietId(Long dailyDietId);
}
