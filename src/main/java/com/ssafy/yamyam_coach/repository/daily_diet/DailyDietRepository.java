package com.ssafy.yamyam_coach.repository.daily_diet;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.repository.daily_diet.request.DailyDietUpdateRequest;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyDietRepository {

    int insert(DailyDiet  dailyDiet);

    Optional<DailyDiet> findById(Long dailyDietId);

    boolean existsByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    Optional<DailyDiet> findByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    Optional<DailyDietDetail> findDetailByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    int updateDailyDiet(DailyDietUpdateRequest request);

    List<DailyDiet> findByDietPlan(Long dietPlanId);

    int deleteById(Long dailyDietId);

    int deleteByDietPlanAndDateInBatch(Long dietPlanId, List<LocalDate> datesToDelete);

    List<DailyDiet> findAllById(List<Long> ids);

    List<DailyDietDetail> findAllByIds(List<Long> ids);
}
