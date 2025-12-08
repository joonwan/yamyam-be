package com.ssafy.yamyam_coach.service.daily_diet;

import com.ssafy.exception.daily_diet.DailyDietException;
import com.ssafy.exception.diet_plan.DietPlanException;
import com.ssafy.exception.food.FoodException;
import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.food.BaseUnit;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import com.ssafy.yamyam_coach.repository.daily_diet.response.MealDetail;
import com.ssafy.yamyam_coach.repository.daily_diet.response.MealFoodDetail;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.food.FoodRepository;
import com.ssafy.yamyam_coach.repository.meal.MealRepository;
import com.ssafy.yamyam_coach.repository.mealfood.MealFoodRepository;
import com.ssafy.yamyam_coach.service.daily_diet.request.CreateDailyDietServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.request.CreateMealFoodServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietDetailServiceResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.MealFoodDetailServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ssafy.exception.daily_diet.ErrorCode.*;
import static com.ssafy.exception.diet_plan.ErrorCode.*;
import static com.ssafy.exception.food.ErrorCode.*;
import static com.ssafy.yamyam_coach.domain.meals.MealType.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DailyDietService {

    private final DietPlanRepository dietPlanRepository;
    private final DailyDietRepository dailyDietRepository;
    private final MealRepository mealRepository;
    private final FoodRepository foodRepository;
    private final MealFoodRepository mealFoodRepository;

    @Transactional
    public void registerDailyDiet(CreateDailyDietServiceRequest request) {

        log.debug("[DailyDietService.registerDailyDiet]: request = {}", request);

        // 1. DietPlan 존재 여부 검증
        DietPlan dietPlan = dietPlanRepository.findById(request.getDietPlanId())
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        // 2. 날짜 유효성 검증 (범위 및 중복 확인)
        validateDate(dietPlan, request.getDate());

        log.debug("diet plan = {}" , dietPlan.getId());
        log.debug("description = {}", request.getDescription());

        // 3. 요청된 모든 음식 ID 존재 여부 검증
        validateFoodIds(request);

        // 4. DailyDiet 생성 및 저장
        DailyDiet dailyDiet = DailyDiet.builder()
                .dietPlanId(dietPlan.getId())
                .date(request.getDate())
                .description(request.getDescription())
                .build();

        dailyDietRepository.insert(dailyDiet);

        // 5. 각 식사 타입별 Meal 및 MealFood 생성
        List<MealFood> breakfastFoods = processMealType(request.getBreakfast(), BREAKFAST, dailyDiet.getId());
        List<MealFood> lunchFoods = processMealType(request.getLunch(), LUNCH, dailyDiet.getId());
        List<MealFood> dinnerFoods = processMealType(request.getDinner(), DINNER, dailyDiet.getId());
        List<MealFood> snackFoods = processMealType(request.getSnack(), SNACK, dailyDiet.getId());

        List<MealFood> mealFoods = Stream.of(breakfastFoods, lunchFoods, dinnerFoods, snackFoods)
                .flatMap(List::stream)
                .toList();

        // 6. MealFood 일괄 저장
        if (!mealFoods.isEmpty()) {
            mealFoodRepository.batchInsert(mealFoods);
        }
    }

    public DailyDietDetailServiceResponse findByDietPlanIdAndDate(Long dietPlanId, LocalDate date) {

        // 1. dietPlanId 존재 검증
        boolean isExists = dietPlanRepository.existsById(dietPlanId);
        if (!isExists) {
            throw new DietPlanException(NOT_FOUND_DIET_PLAN);
        }

        // 2. dietPlanId 및 date 로 dailyDietDetail 조회
        Optional<DailyDietDetail> detailOpt = dailyDietRepository.findDetailByDietPlanIdAndDate(dietPlanId, date);

        // 3. 해당 날의 식단이 없을 경우
        if (detailOpt.isEmpty()) {
            return DailyDietDetailServiceResponse.builder().isEmpty(true).build();
        }

        DailyDietDetail detail = detailOpt.get();

        // 4. meals null 체크 및 빈 리스트 처리
        List<MealDetail> meals = detail.getMeals();
        if (meals == null) {
            meals = List.of();
        }

        // 5. 아침 점심 저녁 간식 별로 그룹화
        Map<MealType, MealDetail> mealMap = meals.stream().collect(Collectors.toMap(
                MealDetail::getType,
                Function.identity() // 항등함수
        ));

        // 6. 아침 점심 저녁 간식 응답화
        List<MealFoodDetailServiceResponse> breakfastResponse = getMealDetailResponseFrom(mealMap.get(BREAKFAST));
        List<MealFoodDetailServiceResponse> lunchResponse = getMealDetailResponseFrom(mealMap.get(LUNCH));
        List<MealFoodDetailServiceResponse> dinnerResponse = getMealDetailResponseFrom(mealMap.get(DINNER));
        List<MealFoodDetailServiceResponse> snackResponse = getMealDetailResponseFrom(mealMap.get(SNACK));

        log.debug("[DailyDietService.findByDietPlanIdAndDate]: description = {}", detail.getDescription());

        return DailyDietDetailServiceResponse.builder()
                .dailyDietId(detail.getId())
                .isEmpty(false)
                .description(detail.getDescription())
                .breakfast(breakfastResponse)
                .lunch(lunchResponse)
                .dinner(dinnerResponse)
                .snack(snackResponse)
                .build();
    }

    private List<MealFoodDetailServiceResponse> getMealDetailResponseFrom(MealDetail mealDetail) {
        if (mealDetail == null) {
            return List.of();
        }

        List<MealFoodDetail> mealFoods = mealDetail.getMealFoods();
        if (mealFoods == null || mealFoods.isEmpty()) {
            return List.of();
        }

        return mealFoods.stream()
                .map(this::toServiceResponse)
                .toList();
    }

    private MealFoodDetailServiceResponse toServiceResponse(MealFoodDetail mealFoodDetail) {
        MealFoodDetailServiceResponse response = MealFoodDetailServiceResponse.builder()
                .mealFoodId(mealFoodDetail.getId())
                .foodId(mealFoodDetail.getFood().getId())
                .name(mealFoodDetail.getFood().getName())
                .amount(mealFoodDetail.getQuantity())
                .build();

        if (mealFoodDetail.getFood().getBaseUnit() == BaseUnit.g) {
            response.setUnit(BaseUnit.g);
            response.setCaloriePerG(mealFoodDetail.getFood().getEnergyPer100());
        } else {
            response.setUnit(BaseUnit.ml);
            response.setCaloriePerMl(mealFoodDetail.getFood().getEnergyPer100());
        }

        return response;
    }


    private List<MealFood> processMealType(List<CreateMealFoodServiceRequest> foods, MealType mealType, Long dailyDietId) {
        if (foods == null || foods.isEmpty()) {
            return List.of();
        }

        Meal meal = Meal.builder()
                .dailyDietId(dailyDietId)
                .type(mealType)
                .build();

        mealRepository.insert(meal);

        return foods.stream()
                .map(r -> createMealFood(meal.getId(), r))
                .toList();
    }

    private MealFood createMealFood(Long mealId, CreateMealFoodServiceRequest request) {
        return MealFood.builder()
                .mealId(mealId)
                .foodId(request.getFoodId())
                .quantity(request.getAmount())
                .build();
    }

    private void validateDate(DietPlan dietPlan, LocalDate date) {
        // 날짜 범위 검증
        if (date.isBefore(dietPlan.getStartDate()) || date.isAfter(dietPlan.getEndDate())) {
            throw new DailyDietException(INVALID_DATE);
        }

        // 중복 날짜 검증
        if (dailyDietRepository.existsByDietPlanIdAndDate(dietPlan.getId(), date)) {
            throw new DailyDietException(DUPLICATED_DATE);
        }
    }

    private void validateFoodIds(CreateDailyDietServiceRequest request) {
        Set<Long> foodIds = Stream.of(request.getBreakfast(), request.getLunch(), request.getDinner(), request.getSnack())
                .flatMap(List::stream)
                .map(CreateMealFoodServiceRequest::getFoodId)
                .collect(Collectors.toSet());

        int existingCount = foodRepository.countExistingIds(foodIds);
        if (existingCount != foodIds.size()) {
            throw new FoodException(NOT_FOUND_FOOD);
        }
    }

}
