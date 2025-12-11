package com.ssafy.yamyam_coach.service.diet_plan;

import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
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

import static com.ssafy.yamyam_coach.exception.diet_plan.ErrorCode.*;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;

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
            throw new DietPlanException(UNAUTHORIZED);
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
        DietPlan primaryDietPlan = dietPlanRepository.findUsersPrimaryDietPlan(currentUserId)
                .orElseThrow(() -> new DietPlanException(NOT_FOUND_PRIMARY_DIET_PLAN));

        return toDietPlanResponse(primaryDietPlan);
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
            throw new DietPlanException(CANNOT_SET_AS_PRIMARY);
        }
    }
}
