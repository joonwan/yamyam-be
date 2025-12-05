package com.ssafy.yamyam_coach.mapper.food;

import com.ssafy.yamyam_coach.domain.food.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FoodMapper {

    List<Food> findByNameLike(String name);

}
