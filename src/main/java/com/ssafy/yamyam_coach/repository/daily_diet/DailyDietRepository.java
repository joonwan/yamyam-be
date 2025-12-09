package com.ssafy.yamyam_coach.repository.daily_diet;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyDietRepository {

    int insert(DailyDiet  dailyDiet);

    Optional<DailyDiet> findById(Long dailyDietId);

    boolean existsByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    Optional<DailyDiet> findByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    Optional<DailyDietDetail> findDetailByDietPlanIdAndDate(Long dietPlanId, LocalDate date);

    int updateDescription(Long dailyDietId, String description);
}
