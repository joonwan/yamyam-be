package com.ssafy.yamyam_coach.repository.daily_diet;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.mapper.daily_diet.DailyDietMapper;
import com.ssafy.yamyam_coach.repository.daily_diet.request.DailyDietUpdateRequest;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
    public int updateDailyDiet(DailyDietUpdateRequest request) {
        return dailyDietMapper.updateDailyDiet(request.getDailyDietId(), request.getDate(), request.getDescription());
    }

    @Override
    public List<DailyDiet> findByDietPlan(Long dietPlanId) {
        return dailyDietMapper.findByDietPlan(dietPlanId);
    }

    @Override
    public int deleteById(Long dailyDietId) {
        return dailyDietMapper.deleteById(dailyDietId);
    }

    @Override
    public int deleteByDietPlanAndDateInBatch(Long dietPlanId, List<LocalDate> datesToDelete) {
        if (datesToDelete == null || datesToDelete.isEmpty()) {
            return 0;
        }
        
        return dailyDietMapper.deleteByDietPlanAndDateInBatch(dietPlanId, datesToDelete);
    }

    @Override
    public List<DailyDiet> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return dailyDietMapper.findAllById(ids);
    }

    @Override
    public List<DailyDietDetail> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return dailyDietMapper.findAllByIds(ids);
    }

}
