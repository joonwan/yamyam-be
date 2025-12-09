package com.ssafy.yamyam_coach.repository.mealfood;

import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.mapper.mealfood.MealFoodMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisMealFoodRepository implements MealFoodRepository {

    private final MealFoodMapper mealFoodMapper;

    @Override
    public int insert(MealFood mealFood) {
        return mealFoodMapper.insert(mealFood);
    }

    @Override
    public Optional<MealFood> findById(Long mealFoodId) {
        return Optional.ofNullable(mealFoodMapper.findById(mealFoodId));
    }

    @Override
    public int batchInsert(List<MealFood> mealFoods) {
        return mealFoodMapper.batchInsert(mealFoods);
    }
}
