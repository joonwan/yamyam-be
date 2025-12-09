package com.ssafy.yamyam_coach.repository.diet_plan;

import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.mapper.diet_plan.DietPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MybatisDietPlanRepository implements DietPlanRepository {

    private final DietPlanMapper dietPlanMapper;

    @Override
    public int insert(DietPlan dietPlan) {
        return dietPlanMapper.insert(dietPlan);
    }

    @Override
    public Optional<DietPlan> findById(Long dietPlanId) {
        return Optional.ofNullable(dietPlanMapper.findById(dietPlanId));
    }

    @Override
    public List<DietPlan> findDietPlansByUserId(Long userId) {
        return dietPlanMapper.findDietPlansByUserId(userId);
    }

    @Override
    public boolean existsById(Long dietPlanId) {
        return dietPlanMapper.existsById(dietPlanId);
    }

    @Override
    public int deleteById(Long dietPlanId) {
        return dietPlanMapper.deleteById(dietPlanId);
    }

    @Override
    public Optional<DietPlan> findUsersPrimaryDietPlan(Long userId) {
        return Optional.ofNullable(dietPlanMapper.findUsersPrimaryDietPlan(userId));
    }

    @Override
    public int deActivateCurrentPrimaryDietPlan(Long userId) {
        return dietPlanMapper.deActivateCurrentPrimaryDietPlan(userId);
    }

    @Override
    public int activateCurrentPrimaryDietPlan(Long userId, Long dietPlanId) {
        return dietPlanMapper.activateCurrentPrimaryDietPlan(userId, dietPlanId);
    }

}
