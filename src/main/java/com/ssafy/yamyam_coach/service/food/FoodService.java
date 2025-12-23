package com.ssafy.yamyam_coach.service.food;

import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.repository.food.FoodRepository;
import com.ssafy.yamyam_coach.service.food.response.SearchFoodServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;

    public List<SearchFoodServiceResponse> searchFood(String name) {

        return foodRepository.findByNameLike(name)
                .stream()
                .map(this::toSearchFoodServiceResponse)
                .toList();
    }

    public List<Food> findAllPaged(int size, int offset) {
        return foodRepository.findAllPaged(size, offset);
    }

    private SearchFoodServiceResponse toSearchFoodServiceResponse(Food food) {
        return SearchFoodServiceResponse.builder()
                .foodId(food.getId())
                .name(food.getName())
                .category(food.getCategory())
                .baseUnit(food.getBaseUnit())
                .caloriePer100(food.getEnergyPer100())
                .proteinPer100(food.getProteinPer100())
                .carbohydratePer100(food.getCarbohydratePer100())
                .fatPer100(food.getFatPer100())
                .sugarPer100(food.getSugarPer100())
                .sodiumPer100(food.getSodiumPer100())
                .build();
    }
}
