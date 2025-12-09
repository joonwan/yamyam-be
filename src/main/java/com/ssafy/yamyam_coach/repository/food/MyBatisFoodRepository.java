package com.ssafy.yamyam_coach.repository.food;

import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.mapper.food.FoodMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MyBatisFoodRepository implements FoodRepository {

    private final FoodMapper foodMapper;

    @Override
    public int insert(Food food) {
        return foodMapper.insert(food);
    }

    @Override
    public Optional<Food> findById(Long foodId) {
        return Optional.ofNullable(foodMapper.findById(foodId));
    }

    @Override
    public List<Food> findByNameLike(String name) {
        log.debug("[MybatisFoodRepository.findByNameLike]: 검색 시작, name = {}", name);
        return foodMapper.findByNameLike(name);
    }

    @Override
    public int countExistingIds(Set<Long> foodIds) {
        if (foodIds.isEmpty()) {
            return 0;
        }
        return foodMapper.countExistingIds(foodIds);
    }
}
