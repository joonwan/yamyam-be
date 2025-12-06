package com.ssafy.yamyam_coach.repository.diet_plan;

import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.mapper.diet_plan.DietPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MybatisDietPlanRepository implements DietPlanRepository {

    private final DietPlanMapper dietPlanMapper;

    @Override
    public int save(DietPlan dietPlan) {
        return dietPlanMapper.insert(dietPlan);
    }

    @Override
    public DietPlan findById(Long dietPlanId) {
        return dietPlanMapper.findById(dietPlanId);
    }

    @Override
    public List<DietPlan> findDietPlansByUserId(Long userId) {
        return dietPlanMapper.findDietPlansByUserId(userId);
    }
}
