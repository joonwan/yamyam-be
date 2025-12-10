package com.ssafy.yamyam_coach.repository.daily_diet;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.food.FoodRepository;
import com.ssafy.yamyam_coach.repository.meal.MealRepository;
import com.ssafy.yamyam_coach.repository.mealfood.MealFoodRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static com.ssafy.yamyam_coach.util.DomainAssertions.assertDailyDietEquals;
import static com.ssafy.yamyam_coach.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class DailyDietRepositoryTest extends IntegrationTestSupport {

    @Autowired
    DailyDietRepository dailyDietRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DietPlanRepository dietPlanRepository;

    @Autowired
    MealRepository mealRepository;

    @Autowired
    MealFoodRepository mealFoodRepository;

    @Autowired
    FoodRepository foodRepository;

    @DisplayName("일일 식단을 저장할 수 있다.")
    @Test
    void insert() {
        //given
        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
        dailyDietRepository.insert(dailyDiet);

        //when
        DailyDiet findDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);

        //then
        assertThat(findDailyDiet).isNotNull();
        assertDailyDietEquals(findDailyDiet, dailyDiet);
    }

    @DisplayName("식단 계획 ID와 날짜로 일일 식단 존재 여부를 확인할 수 있다.")
    @Test
    void existsByDietPlanIdAndDate() {
        //given
        User user = createDummyUser();
        userRepository.save(user);

        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = startDate.plusDays(2);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), startDate, endDate);
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "description");
        dailyDietRepository.insert(dailyDiet);

        //when
        boolean isExists = dailyDietRepository.existsByDietPlanIdAndDate(dietPlan.getId(), startDate);

        //then
        assertThat(isExists).isTrue();
    }

    @DisplayName("식단 계획 ID와 날짜로 일일 식단을 조회할 수 있다.")
    @Test
    void findByDietPlanIdAndDate() {
        //given
        User user = createDummyUser();
        userRepository.save(user);

        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = startDate.plusDays(2);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), startDate, endDate);
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "description");
        dailyDietRepository.insert(dailyDiet);

        //when
        DailyDiet findDailyDiet = dailyDietRepository.findByDietPlanIdAndDate(dietPlan.getId(), startDate).orElse(null);

        //then
        assertThat(findDailyDiet).isNotNull();
        assertDailyDietEquals(findDailyDiet, dailyDiet);
    }

    @DisplayName("식단 계획 ID와 날짜로 일일 식단 상세 정보를 조회할 수 있다.")
    @Test
    void findDetailByDietPlanIdAndDate() {
        //given

        // 1. User 생성
        User user = createDummyUser();
        userRepository.save(user);

        // 2. DietPlan 생성
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = startDate.plusDays(7);
        DietPlan dietPlan = createDummyDietPlan(user.getId(), startDate, endDate);
        dietPlanRepository.insert(dietPlan);

        // 3. DailyDiet 생성
        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "건강한 하루 식단");
        dailyDietRepository.insert(dailyDiet);

        // 4. Food 생성
        Food food1 = createDummyFood(); // 닭가슴살
        Food food2 = createDummyFoods10().get(1); // 현미밥
        foodRepository.insert(food1);
        foodRepository.insert(food2);

        // 5. Meal 생성 (아침, 점심)
        Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
        Meal lunch = createMeal(dailyDiet.getId(), MealType.LUNCH);
        mealRepository.insert(breakfast);
        mealRepository.insert(lunch);

        // 6. MealFood 생성
        MealFood breakfastFood1 = createMealFood(breakfast.getId(), food1.getId(), 200.0);
        MealFood lunchFood1 = createMealFood(lunch.getId(), food2.getId(), 150.0);
        mealFoodRepository.insert(breakfastFood1);
        mealFoodRepository.insert(lunchFood1);

        //when
        DailyDietDetail detail = dailyDietRepository.findDetailByDietPlanIdAndDate(dietPlan.getId(), startDate).orElse(null);

        //then
        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(dailyDiet.getId());
        assertThat(detail.getDietPlanId()).isEqualTo(dietPlan.getId());
        assertThat(detail.getDate()).isEqualTo(startDate);
        assertThat(detail.getDescription()).isEqualTo("건강한 하루 식단");
        assertThat(detail.getMeals()).isNotNull();
        assertThat(detail.getMeals()).hasSize(2);
    }

    @DisplayName("일일 식단의 설명을 수정할 수 있다.")
    @Test
    void updateDescription() {
        //given
        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
        dailyDietRepository.insert(dailyDiet);

        //when
        String nextDescription = "description2";
        dailyDietRepository.updateDescription(dailyDiet.getId(), nextDescription);
        DailyDiet updatedDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);
        //then
        assertThat(updatedDailyDiet).isNotNull();
        assertThat(updatedDailyDiet.getDescription()).isEqualTo(nextDescription);
    }

    @DisplayName("diet plan id 로 날짜순으로 정렬된 daily diet 들을 조회할 수 있다.")
    @Test
    void findByDietPlanId() {
        // given
        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet1 = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description1");
        dailyDietRepository.insert(dailyDiet1);

        DailyDiet dailyDiet2 = createDailyDiet(dietPlan.getId(), LocalDate.now().plusDays(1), "description2");
        dailyDietRepository.insert(dailyDiet2);

        DailyDiet dailyDiet3 = createDailyDiet(dietPlan.getId(), LocalDate.now().plusDays(2), "description3");
        dailyDietRepository.insert(dailyDiet3);

        // when
        List<DailyDiet> dailyDiets = dailyDietRepository.findByDietPlan(dietPlan.getId());

        //then
        assertThat(dailyDiets).hasSize(3)
                .extracting(DailyDiet::getDate)
                .containsExactly(dailyDiet1.getDate(), dailyDiet2.getDate(), dailyDiet3.getDate())
                .isSortedAccordingTo(LocalDate::compareTo);

    }

    @DisplayName("daily diet 의 날짜를 업데이트 할 수 있다.")
    @Test
    void updateDate() {

        // given

        LocalDate before = LocalDate.now();
        LocalDate after = LocalDate.now().plusDays(1);

        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), before, after);
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), after,"description1");
        dailyDietRepository.insert(dailyDiet);

        // when
        dailyDietRepository.updateDate(dailyDiet.getId(), after);
        DailyDiet findDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);

        //then
        assertThat(findDailyDiet).isNotNull();
        assertThat(findDailyDiet.getDate()).isEqualTo(after);
    }

    @DisplayName("id 기반으로 일일 식단을 삭제할 수 있다.")
    @Test
    void deleteById() {
        // given
        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description1");
        dailyDietRepository.insert(dailyDiet);

        //when
        dailyDietRepository.deleteById(dailyDiet.getId());
        DailyDiet findDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);

        //then
        assertThat(findDailyDiet).isNull();
    }
}