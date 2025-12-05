package com.ssafy.yamyam_coach.controller.food;

import com.ssafy.yamyam_coach.service.food.FoodService;
import com.ssafy.yamyam_coach.service.food.response.SearchFoodServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping("/search")
    public ResponseEntity<List<SearchFoodServiceResponse>> searchFood(@RequestParam String name) {
        log.debug("[FoodController.searchFood]: 메서드 수행 시작");
        log.debug("name: {}", name);

        return ResponseEntity.ok(foodService.searchFood(name.trim()));
    }

}
