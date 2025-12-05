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

        List<Food> results = foodRepository.findByNameLike(name);

        Food food = results.get(0);

        log.debug("name = {}", food.getName());
        log.debug("result size = {}", results.size());

        return foodRepository.findByNameLike(name)
                .stream()
                .map(this::toSearchFoodServiceResponse)
                .toList();
    }

    private SearchFoodServiceResponse toSearchFoodServiceResponse(Food food) {
        SearchFoodServiceResponse response = SearchFoodServiceResponse.builder()
                .name(food.getName())
                .category(food.getCategory())
                .build();

        if (food.getBaseUnit().name().equals("g")) {
            response.setCaloriePerG(food.getEnergyPer100());
        }

        if (food.getBaseUnit().name().equals("ml")) {
            response.setCaloriePerMl(food.getEnergyPer100());
        }

        return response;
    }
}
