package com.ssafy.yamyam_coach.repository.diet_plan;

import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;

import java.util.List;

public interface DietPlanRepository {

    int save(DietPlan dietPlan);

    DietPlan findById(Long dietPlanId);

    List<DietPlan> findDietPlansByUserId(Long userId);
}
