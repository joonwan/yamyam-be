package com.ssafy.yamyam_coach.repository.food;

import com.ssafy.yamyam_coach.domain.food.Food;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FoodRepository {

    int insert(Food food);

    Optional<Food> findById(Long foodId);

    List<Food> findByNameLike(String name);

    int countExistingIds(Set<Long> foodIds);
}
