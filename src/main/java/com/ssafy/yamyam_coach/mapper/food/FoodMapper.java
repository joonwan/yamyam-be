package com.ssafy.yamyam_coach.mapper.food;

import com.ssafy.yamyam_coach.domain.food.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface FoodMapper {

    List<Food> findByNameLike(String name);

    int countExistingIds(@Param("foodIds") Set<Long> foodIds);

    int insert(Food food);

    Food findById(Long foodId);
}
