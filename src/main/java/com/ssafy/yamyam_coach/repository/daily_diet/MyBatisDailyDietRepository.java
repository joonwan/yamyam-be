package com.ssafy.yamyam_coach.repository.daily_diet;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.mapper.daily_diet.DailyDietMapper;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisDailyDietRepository implements DailyDietRepository {

    private final DailyDietMapper dailyDietMapper;

    @Override
    public int insert(DailyDiet dailyDiet) {
        return dailyDietMapper.insert(dailyDiet);
    }

    @Override
    public Optional<DailyDiet> findById(Long dailyDietId) {
        return Optional.ofNullable(dailyDietMapper.findById(dailyDietId));
    }

    @Override
    public boolean existsByDietPlanIdAndDate(Long dietPlanId, LocalDate date) {
        return dailyDietMapper.existsByDietPlanIdAndDate(dietPlanId, date);
    }

    @Override
    public Optional<DailyDiet> findByDietPlanIdAndDate(Long dietPlanId, LocalDate date) {
        return Optional.ofNullable(dailyDietMapper.findByDietPlanIdAndDate(dietPlanId, date));
    }

    @Override
    public Optional<DailyDietDetail> findDetailByDietPlanIdAndDate(Long dietPlanId, LocalDate date) {
        return Optional.ofNullable(dailyDietMapper.findDetailByDietPlanIdAndDate(dietPlanId, date));
    }

    @Override
    public int updateDescription(Long dailyDietId, String description) {
        return dailyDietMapper.updateDescription(dailyDietId, description);
    }

}
