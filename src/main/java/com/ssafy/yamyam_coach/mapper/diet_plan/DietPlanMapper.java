package com.ssafy.yamyam_coach.mapper.diet_plan;

import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DietPlanMapper {

    int insert(DietPlan dietPlan);

    DietPlan findById(Long dietPlanId);

    List<DietPlan> findDietPlansByUserId(Long userId);
}
