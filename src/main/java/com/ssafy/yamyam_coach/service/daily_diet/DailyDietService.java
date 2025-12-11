package com.ssafy.yamyam_coach.service.daily_diet;

import com.ssafy.yamyam_coach.exception.daily_diet.DailyDietException;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.daily_diet.request.DailyDietUpdateRequest;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import com.ssafy.yamyam_coach.repository.daily_diet.response.MealDetail;
import com.ssafy.yamyam_coach.repository.daily_diet.response.MealFoodDetail;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.service.daily_diet.request.DailyDietDetailServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.request.DailyDietUpdateServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.request.RegisterDailyDietServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietDetailResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietsResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.MealFoodDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ssafy.yamyam_coach.domain.meals.MealType.*;
import static com.ssafy.yamyam_coach.exception.daily_diet.ErrorCode.*;
import static com.ssafy.yamyam_coach.exception.diet_plan.ErrorCode.NOT_FOUND_DIET_PLAN;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DailyDietService {

    private final DietPlanRepository dietPlanRepository;
    private final DailyDietRepository dailyDietRepository;

    @Transactional
    public Long registerDailyDiet(Long currentUserId, RegisterDailyDietServiceRequest request) {

        // 1. Diet plan 조회 및 존재 검증
        DietPlan dietPlan = dietPlanRepository.findById(request.getDietPlanId())
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        // 2. diet plan 이 현재 사용자의 것인지 검증
        validateUser(currentUserId, dietPlan.getUserId());

        // 3. date 가 DietPlan 기간 내인지 검증
        validateDate(dietPlan, request.getDate());

        // 4. 중복된 date 가 있는지 검증
        validateDuplication(dietPlan, request.getDate());

        // 5. daily diet 생성
        DailyDiet dailyDiet = DailyDiet.builder()
                .dietPlanId(request.getDietPlanId())
                .date(request.getDate())
                .description(request.getDescription())
                .build();

        // 6. daily diet 저장
        dailyDietRepository.insert(dailyDiet);
        return dailyDiet.getId();
    }

    public DailyDietsResponse getDailyDietByDietPlan(Long dietPlanId) {

        // 1. diet plan 존재 검증
        if (!dietPlanRepository.existsById(dietPlanId)) {
            throw new DietPlanException(NOT_FOUND_DIET_PLAN);
        }

        // 2. diet plan 과 연결된 daily diet 모두 조회 후 응답 형식으로 변환
        List<DailyDietResponse> dailyDietResponses = dailyDietRepository.findByDietPlan(dietPlanId).stream()
                .map(this::toDailyDietResponse)
                .toList();

        // 3. 반환
        return DailyDietsResponse.builder().dailyDiets(dailyDietResponses).build();
    }

    public DailyDietDetailResponse getDailyDietDetailByDietPlan(DailyDietDetailServiceRequest request) {

        // 1. diet plan 과 daily diet join 한 결과 불러오기
        DailyDietDetail dailyDietDetail = dailyDietRepository.findDetailByDietPlanIdAndDate(request.getDietPlanId(), request.getDate())
                .orElseThrow(() -> new DailyDietException(NOT_FOUND_DAILY_DIET));


        // 2. 아침 점심 저녁 간식 별 식단의 음식 조회
        Map<MealType, MealDetail> mealDetailByType = dailyDietDetail.getMeals().stream()
                .collect(Collectors.toMap(MealDetail::getType, Function.identity()));

        // 3. 아침에 속한 meal food 들 조회
        List<MealFoodDetailResponse> breakfastDetails = extractMealFoodDetailsByType(mealDetailByType, BREAKFAST);

        // 4. 점심에 속한 meal food 들 조회
        List<MealFoodDetailResponse> lunchDetails = extractMealFoodDetailsByType(mealDetailByType, LUNCH);

        // 4. 저녁에 속한 meal food 들 조회
        List<MealFoodDetailResponse> dinnerDetails = extractMealFoodDetailsByType(mealDetailByType, DINNER);

        // 5. 간식에 속한 meal food 들 조회
        List<MealFoodDetailResponse> snackDetails = extractMealFoodDetailsByType(mealDetailByType, SNACK);

        // 6. 결과 반환
        return DailyDietDetailResponse.builder()
                .dailyDietId(dailyDietDetail.getId())
                .date(dailyDietDetail.getDate())
                .dayOfWeek(dailyDietDetail.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN))
                .description(dailyDietDetail.getDescription())
                .breakfast(breakfastDetails)
                .lunch(lunchDetails)
                .dinner(dinnerDetails)
                .snack(snackDetails)
                .build();
    }

    @Transactional
    public void updateDailyDiet(Long currentUserId, DailyDietUpdateServiceRequest request) {

        // 1. daily diet 가 존재하는지 확인
        DailyDiet dailyDiet = dailyDietRepository.findById(request.getDailyDietId())
                .orElseThrow(() -> new DailyDietException(NOT_FOUND_DAILY_DIET));

        // 2. diet plan 조회
        DietPlan dietPlan = dietPlanRepository.findById(dailyDiet.getDietPlanId())
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        // 3. 현재 사용자와 daily diet 사용자가 동일한지 검증
        validateUser(currentUserId, dietPlan.getUserId());

        // 4. 변경 필요성 확인
        boolean isDescriptionChanged = request.getDescription() != null && !request.getDescription().equals(dailyDiet.getDescription());
        boolean isDateChanged = request.getDate() != null && !request.getDate().equals(dailyDiet.getDate());

        if (!isDescriptionChanged && !isDateChanged) {
            return;
        }

        // 5. 새 날짜가 있을 경우
        if (isDateChanged) {
            // 5.1 diet plan 기간 내인지 검증
            validateDate(dietPlan, request.getDate());

            // 5.2 diet plan 에 중복되는 날짜 있는지 검증
            validateDuplication(dietPlan, request.getDate(), request.getDailyDietId());
        }

        // 6. update
        String newDescription = request.getDescription();
        LocalDate newDate = request.getDate();

        if (!isDescriptionChanged) {
            newDescription = dailyDiet.getDescription();
        }

        if (!isDateChanged) {
            newDate = dailyDiet.getDate();
        }

        DailyDietUpdateRequest updateRequest = DailyDietUpdateRequest.builder()
                .dailyDietId(request.getDailyDietId())
                .description(newDescription)
                .date(newDate)
                .build();

        dailyDietRepository.updateDailyDiet(updateRequest);
    }

    @Transactional
    public void deleteDailyDiet(Long currentUserId, Long dailyDietId) {

        // 1.daily diet 조회 및 존재 검증
        DailyDiet dailyDiet = dailyDietRepository.findById(dailyDietId)
                .orElseThrow(() -> new DailyDietException(NOT_FOUND_DAILY_DIET));

        // 2. diet plan 조회 및 존재 검증
        DietPlan dietPlan = dietPlanRepository.findById(dailyDiet.getDietPlanId())
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        // 3. 요청자와 diet plan 소유자가 동일한 지 검증
        validateUser(currentUserId, dietPlan.getUserId());

        // 4. 삭제
        dailyDietRepository.deleteById(dailyDietId);
    }

    private void validateDate(DietPlan dietPlan, LocalDate date) {
        if (isOutOfDate(dietPlan, date)) {
            throw new DailyDietException(INVALID_DATE);
        }
    }

    private static boolean isOutOfDate(DietPlan dietPlan, LocalDate date) {
        return date.isBefore(dietPlan.getStartDate()) || date.isAfter(dietPlan.getEndDate());
    }

    private void validateDuplication(DietPlan dietPlan, LocalDate date) {
        if (alreadyHasDailyDiet(dietPlan, date)) {
            throw new DailyDietException(DUPLICATED_DATE);
        }
    }

    private void validateDuplication(DietPlan dietPlan, LocalDate date, Long dailyDietId) {
        if (alreadyHasDailyDiet(dietPlan, date, dailyDietId)) {
            throw new DailyDietException(DUPLICATED_DATE);
        }
    }

    // 날짜 변경 대상 날짜에 이미 daily diet 가 있으면 true return
    private boolean alreadyHasDailyDiet(DietPlan dietPlan, LocalDate date) {
        return dailyDietRepository.findByDietPlanIdAndDate(dietPlan.getId(), date).isPresent();
    }

    // 날짜 변경 대상 날짜에 이미 daily diet 가 있고 현재 daily diet 가 동일하지 않을 경우 true return
    private boolean alreadyHasDailyDiet(DietPlan dietPlan, LocalDate date, Long originalDailyDietId) {
        Optional<DailyDiet> findDailyDietOpt = dailyDietRepository.findByDietPlanIdAndDate(dietPlan.getId(), date);
        return findDailyDietOpt.isPresent() && !findDailyDietOpt.get().getId().equals(originalDailyDietId);
    }

    private DailyDietResponse toDailyDietResponse(DailyDiet dailyDiet) {
        return DailyDietResponse.builder()
                .dailyDietId(dailyDiet.getId())
                .date(dailyDiet.getDate())
                .dayOfWeek(dailyDiet.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN))
                .description(dailyDiet.getDescription())
                .build();
    }

    private MealFoodDetailResponse toMealFoodDetailResponse(MealFoodDetail mealFoodDetail) {
        return MealFoodDetailResponse.builder()
                .foodId(mealFoodDetail.getFood().getId())
                .foodName(mealFoodDetail.getFood().getName())
                .mealFoodID(mealFoodDetail.getId())
                .quantity(mealFoodDetail.getQuantity())
                .unit(mealFoodDetail.getFood().getBaseUnit())
                .energyPer100(mealFoodDetail.getFood().getEnergyPer100())
                .build();
    }

    private void validateUser(Long currentUserId, Long userId) {
        if (!currentUserId.equals(userId)) {
            throw new DailyDietException(UNAUTHORIZED);
        }
    }

    private List<MealFoodDetailResponse> extractMealFoodDetailsByType(Map<MealType, MealDetail> mealDetailByType, MealType type) {
        if (mealDetailByType.get(type) == null) {
            return List.of();
        }

        return mealDetailByType.get(type).getMealFoods().stream()
                .map(this::toMealFoodDetailResponse)
                .toList();
    }
}
