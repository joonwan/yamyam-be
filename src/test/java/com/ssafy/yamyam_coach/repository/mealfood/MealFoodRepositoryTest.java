package com.ssafy.yamyam_coach.repository.mealfood;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.food.FoodRepository;
import com.ssafy.yamyam_coach.repository.meal.MealRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static com.ssafy.yamyam_coach.repository.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class MealFoodRepositoryTest extends IntegrationTestSupport {

    @Autowired
    MealFoodRepository mealFoodRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DietPlanRepository dietPlanRepository;

    @Autowired
    DailyDietRepository dailyDietRepository;

    @Autowired
    MealRepository mealRepository;

    @Autowired
    FoodRepository foodRepository;

    @DisplayName("meal food를 저장할 수 있다.")
    @Test
    void insert() {
        //given

        User user = createDummyUser();
        userRepository.insert(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
        dailyDietRepository.insert(dailyDiet);

        Meal meal = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
        mealRepository.insert(meal);

        Food food = createDummyFood();
        foodRepository.insert(food);

        MealFood mealFood = createMealFood(meal.getId(), food.getId(), 100.1);

        //when
        mealFoodRepository.insert(mealFood);
        MealFood findMealFood = mealFoodRepository.findById(mealFood.getId()).orElse(null);

        //then
        assertThat(findMealFood).isNotNull();
        assertMealFoodEquals(findMealFood, mealFood);

    }

    @DisplayName("여러 meal food 들을 배치로 저장할 수 있다.")
    @Test
    void batchInsert() {
        //given

        User user = createDummyUser();
        userRepository.insert(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
        dailyDietRepository.insert(dailyDiet);

        Meal meal = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
        mealRepository.insert(meal);

        List<Food> foods = createDummyFoods10();

        Food food1 = foods.get(0);
        Food food2 = foods.get(1);

        foodRepository.insert(food1);
        foodRepository.insert(food2);

        //when
        MealFood mealFood1 = createMealFood(meal.getId(), food1.getId(), 100.1);
        MealFood mealFood2 = createMealFood(meal.getId(), food2.getId(), 100.2);
        mealFoodRepository.batchInsert(List.of(mealFood1, mealFood2));

        MealFood findMealFood1 = mealFoodRepository.findById(mealFood1.getId()).orElse(null);
        MealFood findMealFood2 = mealFoodRepository.findById(mealFood2.getId()).orElse(null);

        //then
        assertMealFoodEquals(findMealFood1, mealFood1);
        assertMealFoodEquals(findMealFood2, mealFood2);

    }

    private void assertMealFoodEquals(MealFood actual, MealFood expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.getMealId()).isEqualTo(expected.getMealId());
        assertThat(actual.getFoodId()).isEqualTo(expected.getFoodId());
        assertThat(actual.getQuantity()).isEqualTo(expected.getQuantity());
    }

}