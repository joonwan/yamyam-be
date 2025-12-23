package com.ssafy.yamyam_coach.mapper.daily_diet;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.repository.daily_diet.request.DailyDietUpdateRequest;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyDietMapper {

    int insert(DailyDiet dailyDiet);

    DailyDiet findById(Long dailyDietId);

    boolean existsByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    DailyDiet findByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    DailyDietDetail findDetailByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    List<DailyDiet> findByDietPlan(Long dietPlanId);

    int updateDailyDiet(@Param("dailyDietId") Long dailyDietId, @Param("date") LocalDate date, @Param("description") String description);

    int deleteById(Long dailyDietId);

    int deleteByDietPlanAndDateInBatch(@Param("dietPlanId") Long dietPlanId, @Param("datesToDelete") List<LocalDate> datesToDelete);

    List<DailyDiet> findAllById(@Param("ids") List<Long> ids);

    List<DailyDietDetail> findAllByIds(@Param("ids") List<Long> ids);
}
