package com.ssafy.yamyam_coach.repository.food;

import com.ssafy.yamyam_coach.domain.food.Food;

import java.util.List;

public interface FoodRepository {

    List<Food> findByNameLike(String name);

}
