package com.ssafy.yamyam_coach.repository;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.food.BaseUnit;
import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.domain.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public abstract class TestFixtures {

    public static User createUser(String name, String nickname, String email, String password) {
        return User.builder()
                .name(name)
                .nickname(nickname)
                .email(email)
                .password(password)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static User createDummyUser() {
        return createUser("홍길동", "길동이", "test@example.com", "password123");
    }

    public static DietPlan createDietPlan(Long userId, String title, String content, boolean isShared, boolean isPrimary, LocalDate startDate, LocalDate endDate) {
        return DietPlan.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .isShared(isShared)
                .isPrimary(isPrimary)
                .startDate(startDate)
                .endDate(endDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static DietPlan createDummyDietPlan(Long userId, LocalDate startDate, LocalDate endDate) {
        return createDietPlan(
                userId,
                "다이어트 식단",
                "건강한 다이어트를 위한 식단 계획",
                false,
                true,
                startDate,
                endDate
        );
    }

    public static DailyDiet createDailyDiet(Long dietPlanId, LocalDate date, String description) {
        return DailyDiet.builder()
                .dietPlanId(dietPlanId)
                .date(date)
                .description(description)
                .build();
    }

    public static Meal createMeal(Long dailyDietId, MealType type) {
        return Meal.builder()
                .dailyDietId(dailyDietId)
                .type(type)
                .build();
    }

    public static MealFood createMealFood(Long mealId, Long foodId, Double quantity) {
        return MealFood.builder()
                .mealId(mealId)
                .foodId(foodId)
                .quantity(quantity)
                .build();
    }

    public static Food createFood(String name, String category, BaseUnit baseUnit,
                                  Double energyPer100, Double proteinPer100, Double fatPer100,
                                  Double carbohydratePer100, Double sugarPer100, Double sodiumPer100,
                                  Double cholesterolPer100, Double saturatedFatPer100, Double transFatPer100) {
        return Food.builder()
                .name(name)
                .category(category)
                .baseUnit(baseUnit)
                .energyPer100(energyPer100)
                .proteinPer100(proteinPer100)
                .fatPer100(fatPer100)
                .carbohydratePer100(carbohydratePer100)
                .sugarPer100(sugarPer100)
                .sodiumPer100(sodiumPer100)
                .cholesterolPer100(cholesterolPer100)
                .saturatedFatPer100(saturatedFatPer100)
                .transFatPer100(transFatPer100)
                .build();
    }

    public static Food createDummyFood() {
        return createFood("닭가슴살", "육류", BaseUnit.g,
                165.0, 31.0, 3.6, 0.0, 0.0, 63.0, 85.0, 1.0, 0.0);
    }

    public static List<Food> createDummyFoods10() {
        return List.of(
                // 1. 닭가슴살 (육류) - "닭" 검색용
                createFood("닭가슴살", "육류", BaseUnit.g,
                        165.0, 31.0, 3.6, 0.0, 0.0, 63.0, 85.0, 1.0, 0.0),

                // 2. 현미밥 (곡물) - "밥" 검색용
                createFood("현미밥", "곡물", BaseUnit.g,
                        112.0, 2.6, 0.9, 23.5, 0.2, 1.0, 0.0, 0.2, 0.0),

                // 3. 닭다리살 (육류) - "닭" 검색용
                createFood("닭다리살", "육류", BaseUnit.g,
                        180.0, 27.0, 8.0, 0.0, 0.0, 85.0, 95.0, 2.2, 0.0),

                // 4. 연어 (생선)
                createFood("연어", "생선", BaseUnit.g,
                        208.0, 20.5, 13.4, 0.0, 0.0, 59.0, 55.0, 3.1, 0.0),

                // 5. 계란 (난류)
                createFood("계란", "난류", BaseUnit.g,
                        155.0, 12.6, 10.6, 1.1, 0.6, 124.0, 373.0, 3.3, 0.0),

                // 6. 우유 (유제품) - "우유" 검색용
                createFood("우유", "유제품", BaseUnit.ml,
                        61.0, 3.2, 3.3, 4.8, 4.8, 44.0, 12.0, 1.9, 0.0),

                // 7. 백미밥 (곡물) - "밥" 검색용
                createFood("백미밥", "곡물", BaseUnit.g,
                        130.0, 2.5, 0.3, 28.7, 0.1, 0.0, 0.0, 0.1, 0.0),

                // 8. 닭안심 (육류) - "닭" 검색용
                createFood("닭안심", "육류", BaseUnit.g,
                        114.0, 23.0, 1.2, 0.0, 0.0, 49.0, 58.0, 0.3, 0.0),

                // 9. 두부 (콩류)
                createFood("두부", "콩류", BaseUnit.g,
                        76.0, 8.1, 4.8, 1.9, 0.6, 7.0, 0.0, 0.7, 0.0),

                // 10. 초코우유 (유제품) - "우유" 검색용
                createFood("초코우유", "유제품", BaseUnit.ml,
                        75.0, 2.8, 2.5, 11.2, 10.8, 55.0, 10.0, 1.5, 0.0)
        );
    }

}
