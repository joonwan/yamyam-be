package com.ssafy.yamyam_coach.repository.diet_plan;

import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;

import java.util.List;
import java.util.Optional;

public interface DietPlanRepository {

    int insert(DietPlan dietPlan);

    Optional<DietPlan> findById(Long dietPlanId);

    List<DietPlan> findDietPlansByUserId(Long userId);

    boolean existsById(Long dietPlanId);

    int deleteById(Long dietPlanId);

    Optional<DietPlan> findUsersPrimaryDietPlan(Long userId);

    int deActivateCurrentPrimaryDietPlan(Long userId);

    int activateCurrentPrimaryDietPlan(Long userId, Long dietPlanId);
}
