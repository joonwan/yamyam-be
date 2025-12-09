package com.ssafy.yamyam_coach.mapper.mealfood;

import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MealFoodMapper {

    int insert(MealFood mealFood);

    int batchInsert(@Param("mealFoods") List<MealFood> mealFoods);

    MealFood findById(Long mealFoodId);
}
