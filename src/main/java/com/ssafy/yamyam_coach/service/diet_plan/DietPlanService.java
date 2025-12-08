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
        DietPlan createdDietPlan = createDietPlan(request, getUserIdFromJwtToken());
        dietPlanRepository.save(createdDietPlan);

        return createdDietPlan.getId();
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

    private Long getUserIdFromJwtToken() {
        /**
         * todo jwt token 에서 userId 들고오는 로직 작성 해야함
         */
        return 1L;
    }

    private DietPlan createDietPlan(CreateDietPlanServiceRequest request, Long userId) {
        return DietPlan.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .isShared(false)
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
                .startDate(dietPlan.getStartDate())
                .endDate(dietPlan.getEndDate())
                .build();
    }

}
