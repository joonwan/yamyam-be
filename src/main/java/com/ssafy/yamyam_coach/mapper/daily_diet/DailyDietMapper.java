package com.ssafy.yamyam_coach.mapper.daily_diet;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface DailyDietMapper {

    int insert(DailyDiet dailyDiet);

    DailyDiet findById(Long dailyDietId);

    boolean existsByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    DailyDiet findByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    DailyDietDetail findDetailByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    int updateDescription(@Param("dailyDietId") Long dailyDietId, @Param("description") String description);

}
