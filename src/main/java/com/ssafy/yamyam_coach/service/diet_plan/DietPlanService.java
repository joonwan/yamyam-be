package com.ssafy.yamyam_coach.service.diet_plan;

import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.diet_plan.request.UpdateDietPlanRepositoryRequest;
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

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;
    private final DailyDietRepository dailyDietRepository;

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
