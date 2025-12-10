package com.ssafy.yamyam_coach.util;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.user.User;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class DomainAssertions {

    // DietPlan Assertions
    public static void assertDietPlanEquals(DietPlan actual, DietPlan expected) {
        assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
        assertThat(actual.getTitle()).isEqualTo(expected.getTitle());
        assertThat(actual.getContent()).isEqualTo(expected.getContent());
        assertThat(actual.isShared()).isEqualTo(expected.isShared());
        assertThat(actual.isPrimary()).isEqualTo(expected.isPrimary());
        assertThat(actual.getStartDate()).isEqualTo(expected.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(expected.getEndDate());
        assertThat(actual.getCreatedAt()).isEqualTo(expected.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualTo(expected.getUpdatedAt());
    }

    // Meal Assertions
    public static void assertMealEquals(Meal actual, Meal expected) {
        assertThat(actual.getDailyDietId()).isEqualTo(expected.getDailyDietId());
        assertThat(actual.getType()).isEqualTo(expected.getType());
    }

    // User Assertions
    public static void assertUserEquals(User actual, User expected) {
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getNickname()).isEqualTo(expected.getNickname());
        assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
        assertThat(actual.getPassword()).isEqualTo(expected.getPassword());
    }

    // Food Assertions
    public static void assertFoodEquals(Food actual, Food expected) {
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getCategory()).isEqualTo(expected.getCategory());
        assertThat(actual.getBaseUnit()).isEqualTo(expected.getBaseUnit());
        assertThat(actual.getEnergyPer100()).isEqualTo(expected.getEnergyPer100());
        assertThat(actual.getProteinPer100()).isEqualTo(expected.getProteinPer100());
        assertThat(actual.getFatPer100()).isEqualTo(expected.getFatPer100());
        assertThat(actual.getCarbohydratePer100()).isEqualTo(expected.getCarbohydratePer100());
        assertThat(actual.getSugarPer100()).isEqualTo(expected.getSugarPer100());
        assertThat(actual.getSodiumPer100()).isEqualTo(expected.getSodiumPer100());
        assertThat(actual.getCholesterolPer100()).isEqualTo(expected.getCholesterolPer100());
        assertThat(actual.getSaturatedFatPer100()).isEqualTo(expected.getSaturatedFatPer100());
        assertThat(actual.getTransFatPer100()).isEqualTo(expected.getTransFatPer100());
    }

    // DailyDiet Assertions
    public static void assertDailyDietEquals(DailyDiet actual, DailyDiet expected) {
        assertThat(actual.getDietPlanId()).isEqualTo(expected.getDietPlanId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
    }

    // MealFood Assertions
    public static void assertMealFoodEquals(MealFood actual, MealFood expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.getMealId()).isEqualTo(expected.getMealId());
        assertThat(actual.getFoodId()).isEqualTo(expected.getFoodId());
        assertThat(actual.getQuantity()).isEqualTo(expected.getQuantity());
    }
}
