package com.ssafy.yamyam_coach.service.meal;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.exception.daily_diet.DailyDietException;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.exception.diet_plan.ErrorCode;
import com.ssafy.yamyam_coach.exception.food.FoodException;
import com.ssafy.yamyam_coach.exception.meal.MealException;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.food.FoodRepository;
import com.ssafy.yamyam_coach.repository.meal.MealRepository;
import com.ssafy.yamyam_coach.repository.meal.response.MealDetail;
import com.ssafy.yamyam_coach.repository.mealfood.MealFoodRepository;
import com.ssafy.yamyam_coach.service.meal.request.CreateMealFoodServiceRequest;
import com.ssafy.yamyam_coach.service.meal.request.CreateMealServiceRequest;
import com.ssafy.yamyam_coach.service.meal.request.UpdateMealFoodServiceRequest;
import com.ssafy.yamyam_coach.service.meal.request.UpdateMealServiceRequest;
import com.ssafy.yamyam_coach.service.meal.response.MealDetailServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final DietPlanRepository dietPlanRepository;
    private final DailyDietRepository dailyDietRepository;
    private final MealFoodRepository mealFoodRepository;
    private final FoodRepository foodRepository;

    @Transactional
    public Long createMeal(Long currentUserId, CreateMealServiceRequest request) {

        // 1. dailyDiet 조회 및 존재 검증
        DailyDiet dailyDiet = dailyDietRepository.findById(request.getDailyDietId())
                .orElseThrow(() -> new DailyDietException(com.ssafy.yamyam_coach.exception.daily_diet.ErrorCode.NOT_FOUND_DAILY_DIET));

        // 2. dietPlan Id 조회
        DietPlan dietPlan = dietPlanRepository.findById(dailyDiet.getDietPlanId())
                .orElseThrow(() -> new DietPlanException(ErrorCode.NOT_FOUND_DIET_PLAN));

        // 3. dietPlan 의 소유자가 현재 요청자랑 동일한지 확인
        validateUser(currentUserId, dietPlan.getUserId());

        // 4. dailyDiet 가 현재 요청된 meal type (아침/점심/저녁/간식) 을 가지고 있는지 검증
        validateDuplicatedMealType(dailyDiet, request.getMealType());

        // 5. 요청된 음식들이 db 에 있는지 검증
        validateFoods(request.getMealFoodRequests());

        // 6. Meal 생성 및 저장
        Meal meal = Meal.builder()
                .type(request.getMealType())
                .dailyDietId(dailyDiet.getId())
                .build();

        mealRepository.insert(meal);

        // 7. Meal Food 생성 및 저장
        List<MealFood> mealFoods = request.getMealFoodRequests().stream()
                .map(cmf -> createMealFood(cmf, meal.getId()))
                .toList();

        mealFoodRepository.batchInsert(mealFoods);

        return meal.getId();
    }

    @Transactional
    public void updateMeal(Long currentUserId, UpdateMealServiceRequest request) {
        // 1.meal id 를 통해 meal 이 존재하는지 확인
        Meal meal = mealRepository.findById(request.getMealId())
                .orElseThrow(() -> new MealException(com.ssafy.yamyam_coach.exception.meal.ErrorCode.NOT_FOUND_MEAL));

        // 2. meal 을 통해 daily diet 조회 및 존재 검증
        DailyDiet dailyDiet = dailyDietRepository.findById(meal.getDailyDietId())
                .orElseThrow(() -> new DailyDietException(com.ssafy.yamyam_coach.exception.daily_diet.ErrorCode.NOT_FOUND_DAILY_DIET));

        // 3. daily diet 를 통해 diet plan 조회 및 검증
        DietPlan dietPlan = dietPlanRepository.findById(dailyDiet.getDietPlanId())
                .orElseThrow(() -> new DietPlanException(ErrorCode.NOT_FOUND_DIET_PLAN));

        // 4. diet plan 의 user id 를 통한 현재 요청자와 diet plan 소유자가 동일한지 검증
        validateUser(currentUserId, dietPlan.getUserId());

        // 5. meal type 이 변경될 경우 변경될 meal type 에 이미 다른 meal 이 존재하는지 확인 후 meal type 변경
        if (!meal.getType().equals(request.getMealType())) {
            Optional<Meal> mealOpt = mealRepository.findByDailyDietAndMealType(dailyDiet.getId(), request.getMealType());

            if (mealOpt.isPresent()) {
                throw new MealException(com.ssafy.yamyam_coach.exception.meal.ErrorCode.DUPLICATED_MEAL_TYPE);
            }

            mealRepository.updateMealType(meal.getId(), request.getMealType());
        }

        // 6. 변경될 meal food 들이 있을경우 food 들이 db 에 존재하는지 확인
        validateFoodsForUpdate(request.getMealFoodUpdateRequests());


        // 7. 현재 meal 과 관련된 meal food 들을 모두 삭제하고 새로들어온 food 와 quantity 정보를 이용해 meal food 를 생성하고 batch insert
        mealFoodRepository.deleteByMealId(meal.getId());

        List<MealFood> mealFoods = request.getMealFoodUpdateRequests().stream()
                .map(cmf -> createMealFood(cmf, meal.getId()))
                .toList();

        mealFoodRepository.batchInsert(mealFoods);
    }

    public MealDetailServiceResponse getMealById(Long mealId) {
        // 1. meal 상세 정보 조회 (JOIN으로 meal_food + food 정보 포함)
        MealDetail mealDetail = mealRepository.findMealDetailById(mealId)
                .orElseThrow(() -> new MealException(com.ssafy.yamyam_coach.exception.meal.ErrorCode.NOT_FOUND_MEAL));

        // 2. Service Response로 변환
        return MealDetailServiceResponse.from(mealDetail);
    }

    @Transactional
    public void deleteMeal(Long currentUserId, Long mealId) {
        // 1.meal id 를 통해 meal 이 존재하는지 확인
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new MealException(com.ssafy.yamyam_coach.exception.meal.ErrorCode.NOT_FOUND_MEAL));

        // 2. meal 을 통해 daily diet 조회 및 존재 검증
        DailyDiet dailyDiet = dailyDietRepository.findById(meal.getDailyDietId())
                .orElseThrow(() -> new DailyDietException(com.ssafy.yamyam_coach.exception.daily_diet.ErrorCode.NOT_FOUND_DAILY_DIET));

        // 3. daily diet 를 통해 diet plan 조회 및 검증
        DietPlan dietPlan = dietPlanRepository.findById(dailyDiet.getDietPlanId())
                .orElseThrow(() -> new DietPlanException(ErrorCode.NOT_FOUND_DIET_PLAN));

        // 4. diet plan 의 user id 를 통한 현재 요청자와 diet plan 소유자가 동일한지 검증
        validateUser(currentUserId, dietPlan.getUserId());

        // 5. 삭제 repository 에게 위임 -> cascade 라서 meal food 도 삭제됨
        int deleteCount = mealRepository.deleteById(mealId);

        // 6. return 이 1 미만일 경우 예외
        if (deleteCount < 1) {
            throw new MealException(com.ssafy.yamyam_coach.exception.meal.ErrorCode.NOT_FOUND_MEAL);
        }
    }

    private void validateFoods(List<CreateMealFoodServiceRequest> mealFoodRequests) {
        Set<Long> foodIds = mealFoodRequests.stream()
                .map(CreateMealFoodServiceRequest::getFoodId)
                .collect(Collectors.toSet());

        int existingIds = foodRepository.countExistingIds(foodIds);

        if (foodIds.size() != existingIds) {
            throw new FoodException(com.ssafy.yamyam_coach.exception.food.ErrorCode.NOT_FOUND_FOOD);
        }
    }

    private void validateFoodsForUpdate(List<UpdateMealFoodServiceRequest> mealFoodRequests) {
        Set<Long> foodIds = mealFoodRequests.stream()
                .map(UpdateMealFoodServiceRequest::getFoodId)
                .collect(Collectors.toSet());

        int existingIds = foodRepository.countExistingIds(foodIds);

        if (foodIds.size() != existingIds) {
            throw new FoodException(com.ssafy.yamyam_coach.exception.food.ErrorCode.NOT_FOUND_FOOD);
        }
    }

    private void validateDuplicatedMealType(DailyDiet dailyDiet, MealType mealType) {
        if (mealRepository.existsByDailyDietAndMealType(dailyDiet.getId(), mealType)) {
            throw new MealException(com.ssafy.yamyam_coach.exception.meal.ErrorCode.DUPLICATED_MEAL_TYPE);
        }
    }

    private void validateUser(Long currentUserId, Long userId) {
        if (!currentUserId.equals(userId)) {
            throw new MealException(com.ssafy.yamyam_coach.exception.meal.ErrorCode.UNAUTHORIZED);
        }
    }

    private MealFood createMealFood(CreateMealFoodServiceRequest createMealFoodRequest, Long mealId) {
        return MealFood.builder()
                .foodId(createMealFoodRequest.getFoodId())
                .quantity(createMealFoodRequest.getAmount())
                .mealId(mealId)
                .build();
    }

    private MealFood createMealFood(UpdateMealFoodServiceRequest createMealFoodRequest, Long mealId) {
        return MealFood.builder()
                .foodId(createMealFoodRequest.getFoodId())
                .quantity(createMealFoodRequest.getAmount())
                .mealId(mealId)
                .build();
    }
}
