package com.ssafy.yamyam_coach.service.diet_plan;

import com.ssafy.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.service.diet_plan.request.CreateDietPlanServiceRequest;
import com.ssafy.yamyam_coach.service.diet_plan.response.DietPlanServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.ssafy.exception.diet_plan.ErrorCode.*;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;

    @Transactional
    public Long registerDietPlan(CreateDietPlanServiceRequest request) {
        boolean isPrimary = isDietPlanEmpty(getUserIdFromJwtToken());

        DietPlan createdDietPlan = createDietPlan(request, getUserIdFromJwtToken(), isPrimary);
        dietPlanRepository.insert(createdDietPlan);

        return createdDietPlan.getId();
    }

    @Transactional
    public void deleteById(Long dietPlanId) {
        // 1. 존재 여부 검증
        DietPlan dietPlan = dietPlanRepository.findById(dietPlanId)
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        // 2. 권한 검증 (본인 것만 삭제 가능)
        Long currentUserId = getUserIdFromJwtToken();
        if (!dietPlan.getUserId().equals(currentUserId)) {
            throw new DietPlanException(UNAUTHORIZED);
        }

        // 3. 삭제
        dietPlanRepository.deleteById(dietPlanId);
    }

    @Transactional
    public void changePrimaryDietPlanTo(Long targetId) {

        // 1. 사용자 pk jwt token 에서 추출
        Long userId = getUserIdFromJwtToken();

        // 2. 대표 식단 변경
        updatePrimaryDietPlan(userId, targetId);
    }

    public List<DietPlanServiceResponse> getMyDietPlans() {
        Long userId = getUserIdFromJwtToken();

        return dietPlanRepository.findDietPlansByUserId(userId)
                .stream()
                .map(this::toDietPlanResponse)
                .toList();
    }

    public DietPlanServiceResponse getDietPlanById(Long dietPlanId) {
        DietPlan findDietPlan = dietPlanRepository.findById(dietPlanId)
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        return toDietPlanResponse(findDietPlan);
    }

    public DietPlanServiceResponse getPrimaryDietPlan() {
        Long userId = getUserIdFromJwtToken();
        DietPlan primaryDietPlan = dietPlanRepository.findUsersPrimaryDietPlan(userId)
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

        return toDietPlanResponse(primaryDietPlan);
    }

    private Long getUserIdFromJwtToken() {
        /**
         * todo jwt token 에서 userId 들고오는 로직 작성 해야함
         */
        return 1L;
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
            throw new DietPlanException(NOT_FOUND_DIET_PLAN);
        }
    }
}
