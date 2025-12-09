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

import static com.ssafy.yamyam_coach.repository.TestFixtures.*;
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
        userRepository.insert(user);

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
        userRepository.insert(user);

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
        userRepository.insert(user);

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
        userRepository.insert(user);

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
        userRepository.insert(user);

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

    private void assertDailyDietEquals(DailyDiet actual, DailyDiet expected) {
        assertThat(actual.getDietPlanId()).isEqualTo(expected.getDietPlanId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
    }

}