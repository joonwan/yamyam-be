package com.ssafy.yamyam_coach.service.diet_plan;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import com.ssafy.yamyam_coach.repository.daily_diet.response.MealDetail;
import com.ssafy.yamyam_coach.repository.daily_diet.response.MealFoodDetail;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.diet_plan.request.UpdateDietPlanRepositoryRequest;
import com.ssafy.yamyam_coach.repository.meal.MealRepository;
import com.ssafy.yamyam_coach.repository.mealfood.MealFoodRepository;
import com.ssafy.yamyam_coach.service.diet_plan.request.CreateDietPlanServiceRequest;
import com.ssafy.yamyam_coach.service.diet_plan.request.UpdateDietPlanServiceRequest;
import com.ssafy.yamyam_coach.service.diet_plan.response.DietPlanServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ssafy.yamyam_coach.exception.diet_plan.DietPlanErrorCode.*;
import static com.ssafy.yamyam_coach.exception.post.PostErrorCode.NOT_FOUND_POST;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;
    private final DailyDietRepository dailyDietRepository;
    private final MealRepository mealRepository;
    private final MealFoodRepository mealFoodRepository;

    @Transactional
    public Long registerDietPlan(Long currentUserId, CreateDietPlanServiceRequest request) {
        boolean isPrimary = isDietPlanEmpty(currentUserId);

        DietPlan createdDietPlan = createDietPlan(request, currentUserId, isPrimary);
        dietPlanRepository.insert(createdDietPlan);

        return createdDietPlan.getId();
    }

    @Transactional
    public void deleteById(Long currentUserId, Long dietPlanId) {
        // 1. 존재 여부 검증
        DietPlan dietPlan = dietPlanRepository.findById(dietPlanId)
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        // 2. 권한 검증 (본인 것만 삭제 가능)
        if (!dietPlan.getUserId().equals(currentUserId)) {
            throw new DietPlanException(UNAUTHORIZED_FOR_DELETE);
        }

        // 3. 삭제
        dietPlanRepository.deleteById(dietPlanId);
    }

    @Transactional
    public void changePrimaryDietPlanTo(Long currentUserId, Long targetId) {
        updatePrimaryDietPlan(currentUserId, targetId);
    }

    public List<DietPlanServiceResponse> getMyDietPlans(Long currentUserId) {
        return dietPlanRepository.findDietPlansByUserId(currentUserId)
                .stream()
                .map(this::toDietPlanResponse)
                .toList();
    }

    public DietPlanServiceResponse getDietPlanById(Long dietPlanId) {
        DietPlan findDietPlan = dietPlanRepository.findById(dietPlanId)
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        return toDietPlanResponse(findDietPlan);
    }

    public DietPlanServiceResponse getPrimaryDietPlan(Long currentUserId) {
        Optional<DietPlan> primaryDietPlanOpt = dietPlanRepository.findUsersPrimaryDietPlan(currentUserId);
        if (primaryDietPlanOpt.isEmpty()) {
            return DietPlanServiceResponse.builder()
                    .dietPlanId(null)
                    .title(null)
                    .content(null)
                    .isPrimary(false)
                    .startDate(null)
                    .endDate(null)
                    .build();
        }

        return toDietPlanResponse(primaryDietPlanOpt.get());
    }

    @Transactional
    public void updateDietPlan(Long currentUserId, UpdateDietPlanServiceRequest request) {
        // 1. diet plan 조회
        DietPlan dietPlan = dietPlanRepository.findById(request.getDietPlanId())
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        // 2. 현재 사용자의 diet plan 인지 확인
        validateUser(currentUserId, dietPlan.getUserId());

        // 3. description 변경되었는지 확인
        boolean isContentUpdated = request.getContent() != null && !request.getContent().equals(dietPlan.getContent());

        // 4. 날짜 범위 변경되었는지 확인
        boolean isStartDateUpdated = request.getStartDate() != null && !request.getStartDate().equals(dietPlan.getStartDate());
        boolean isEndDateUpdated = request.getEndDate() != null && !request.getEndDate().equals(dietPlan.getEndDate());

        // 5. 변경 사항이 없을 경우 return

        if (!isContentUpdated && !isStartDateUpdated && !isEndDateUpdated) {
            return;
        }

        // 6. 날짜 변경되었을 경우 새 날짜 범위에 속하지 않는 날짜 추출

        LocalDate startDate = request.getStartDate() == null ? dietPlan.getStartDate() : request.getStartDate();
        LocalDate endDate = request.getEndDate() == null ? dietPlan.getEndDate() : request.getEndDate();

        long duration = java.time.temporal.ChronoUnit.DAYS.between(dietPlan.getStartDate(), dietPlan.getEndDate()) + 1;

        List<LocalDate> datesToDelete = Stream.iterate(dietPlan.getStartDate(), d -> d.plusDays(1))
                .limit(duration)
                .filter(d -> d.isBefore(startDate) || d.isAfter(endDate))
                .toList();

        log.debug("target for delete = {}", datesToDelete);

        // 7. batch delete
        dailyDietRepository.deleteByDietPlanAndDateInBatch(dietPlan.getId(), datesToDelete);

        // 8. update
        UpdateDietPlanRepositoryRequest repositoryRequest = new UpdateDietPlanRepositoryRequest();

        repositoryRequest.setDietPlanId(dietPlan.getId());

        if (isContentUpdated) {
            repositoryRequest.setContent(request.getContent());
        }

        if (isStartDateUpdated) {
            repositoryRequest.setStartDate(request.getStartDate());
        }

        if (isEndDateUpdated) {
            repositoryRequest.setEndDate(request.getEndDate());
        }

        dietPlanRepository.update(repositoryRequest);
    }

    @Transactional
    public Long copyDietPlan(Long currentUserId, Long dietPlanId) {

        // 2. post 기반 연관된 diet plan 조회 및 검증
        DietPlan dietPlan = dietPlanRepository.findById(dietPlanId)
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        // 3. 요청자의 diet plan 이면 return
        if (dietPlan.getUserId().equals(currentUserId)) {
            return dietPlan.getId();
        }

        // 4. diet plan copy 후 저장
        CreateDietPlanServiceRequest createDietPlanRequest = CreateDietPlanServiceRequest.builder()
                .title(dietPlan.getTitle())
                .content(dietPlan.getContent())
                .startDate(dietPlan.getStartDate())
                .endDate(dietPlan.getEndDate())
                .build();

        Long copyDietPlanId = registerDietPlan(currentUserId, createDietPlanRequest);


        // 4. diet plan 기반으로 daily diet detail 들 조회

        long duration = java.time.temporal.ChronoUnit.DAYS.between(dietPlan.getStartDate(), dietPlan.getEndDate()) + 1;

        List<LocalDate> dates = Stream.iterate(dietPlan.getStartDate(), d -> d.plusDays(1))
                .limit(duration)
                .toList();

        // 5. 날짜 순회하면서 DailyDietDetail 들 조회
        for (LocalDate date : dates) {

            Optional<DailyDietDetail> detailOpt = dailyDietRepository.findDetailByDietPlanIdAndDate(dietPlan.getId(), date);
            if (detailOpt.isEmpty()) {
                continue;
            }

            DailyDietDetail dailyDietDetail = detailOpt.get(); // daily diet detail

            // daily diet 복사 후 저장
            DailyDiet copyDailyDiet = DailyDiet.builder()
                    .dietPlanId(copyDietPlanId)
                    .description(dailyDietDetail.getDescription())
                    .date(date)
                    .build();

            dailyDietRepository.insert(copyDailyDiet);

            List<MealDetail> mealDetails = dailyDietDetail.getMeals(); // meal details

            // meal 들 복사 저장
            for (MealDetail mealDetail : mealDetails) {
                Meal copyMeal = Meal.builder()
                        .dailyDietId(copyDailyDiet.getId())
                        .type(mealDetail.getType())
                        .build();

                mealRepository.insert(copyMeal);

                List<MealFoodDetail> mealFoods = mealDetail.getMealFoods();

                // meal food 들 복사 후 저장
                for (MealFoodDetail mealFoodDetail : mealFoods) {
                    MealFood copyMealFood  = MealFood.builder()
                            .mealId(copyMeal.getId())
                            .foodId(mealFoodDetail.getFood().getId())
                            .quantity(mealFoodDetail.getQuantity())
                            .build();

                    mealFoodRepository.insert(copyMealFood);
                }
            }
        }

        return copyDietPlanId;
    }

    private void validateUser(Long currentUserId, Long userId) {
        if (!currentUserId.equals(userId)) {
            throw new DietPlanException(UNAUTHORIZED_FOR_DELETE);
        }
    }


    private DietPlan createDietPlan(CreateDietPlanServiceRequest request, Long userId, boolean isPrimary) {
        return DietPlan.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .isShared(false)
                .isPrimary(isPrimary)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private DietPlanServiceResponse toDietPlanResponse(DietPlan dietPlan) {
        return DietPlanServiceResponse.builder()
                .dietPlanId(dietPlan.getId())
                .authorId(dietPlan.getUserId())
                .title(dietPlan.getTitle())
                .content(dietPlan.getContent())
                .isPrimary(dietPlan.isPrimary())
                .startDate(dietPlan.getStartDate())
                .endDate(dietPlan.getEndDate())
                .build();
    }

    private boolean isDietPlanEmpty(Long userId) {
        return dietPlanRepository.findDietPlansByUserId(userId).isEmpty();
    }

    private void updatePrimaryDietPlan(Long userId, Long newId) {
        dietPlanRepository.deActivateCurrentPrimaryDietPlan(userId);
        int updatedRows = dietPlanRepository.activateCurrentPrimaryDietPlan(userId, newId);

        if (updatedRows == 0) {
            throw new DietPlanException(CANNOT_SET_AS_PRIMARY);
        }
    }
}
