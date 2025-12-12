package com.ssafy.yamyam_coach.repository.meal;

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
import com.ssafy.yamyam_coach.repository.meal.response.MealDetail;
import com.ssafy.yamyam_coach.repository.meal.response.MealFoodDetail;
import com.ssafy.yamyam_coach.repository.mealfood.MealFoodRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.ssafy.yamyam_coach.util.DomainAssertions.*;
import static com.ssafy.yamyam_coach.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class MealRepositoryTest extends IntegrationTestSupport {

    @Autowired
    MealRepository mealRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DietPlanRepository dietPlanRepository;

    @Autowired
    DailyDietRepository dailyDietRepository;

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    MealFoodRepository mealFoodRepository;

    @DisplayName("식사를 저장할 수 있다.")
    @Test
    void insert() {
        //given
        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
        dailyDietRepository.insert(dailyDiet);

        Meal meal = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
        mealRepository.insert(meal);

        //when
        Meal findMeal = mealRepository.findById(meal.getId()).orElse(null);

        //then
        assertThat(findMeal).isNotNull();
        assertMealEquals(findMeal, meal);
    }

    @DisplayName("일일 식단 ID로 해당하는 모든 식사를 삭제할 수 있다.")
    @Test
    void deleteByDailyDietId() {
        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
        dailyDietRepository.insert(dailyDiet);

        Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
        mealRepository.insert(breakfast);

        Meal lunch = createMeal(dailyDiet.getId(), MealType.LUNCH);
        mealRepository.insert(lunch);

        Meal dinner = createMeal(dailyDiet.getId(), MealType.DINNER);
        mealRepository.insert(dinner);

        Meal snack = createMeal(dailyDiet.getId(), MealType.SNACK);
        mealRepository.insert(snack);

        //when
        int deleteCount = mealRepository.deleteByDailyDietId(dailyDiet.getId());
        Optional<Meal> breakfastOpt = mealRepository.findById(breakfast.getId());
        Optional<Meal> lunchOpt = mealRepository.findById(lunch.getId());
        Optional<Meal> dinnerOpt = mealRepository.findById(dinner.getId());
        Optional<Meal> snackOpt = mealRepository.findById(snack.getId());

        //then
        assertThat(deleteCount).isEqualTo(4);
        assertThat(breakfastOpt).isNotPresent();
        assertThat(lunchOpt).isNotPresent();
        assertThat(dinnerOpt).isNotPresent();
        assertThat(snackOpt).isNotPresent();
    }

    @Nested
    @DisplayName("existsByDailyDietAndMealType")
    class ExistsByDailyDietAndMealType {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("특정 타입의 식사가 이미 있을 경우 true 를 반환한다.")
            @Test
            void alreadyExistMealType() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                // when
                boolean isExists = mealRepository.existsByDailyDietAndMealType(dailyDiet.getId(), MealType.BREAKFAST);

                // then
                assertThat(isExists).isTrue();
            }

            @DisplayName("특정 타입의 식사가 없을 경우 false 를 반환한다.")
            @Test
            void notExistMealType() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
                dailyDietRepository.insert(dailyDiet);

                // when
                boolean isExists = mealRepository.existsByDailyDietAndMealType(dailyDiet.getId(), MealType.BREAKFAST);

                // then
                assertThat(isExists).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("findByDailyDietAndMealType")
    class FindByDailyDietAndMealType {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("daily diet id 와 meal type 을 통해 meal 을 조회할 수 있다.")
            @Test
            void findByDailyDietAndMealType() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                // when
                Meal findBreakfast = mealRepository.findByDailyDietAndMealType(dailyDiet.getId(), MealType.BREAKFAST).orElse(null);

                // then
                assertThat(findBreakfast).isNotNull();
                assertMealEquals(findBreakfast, breakfast);

            }

            @DisplayName("해당 meal type 이 없을 경우 빈 optional 이 반환된다.")
            @Test
            void returnEmptyOptional() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
                dailyDietRepository.insert(dailyDiet);



                // when
                Meal findBreakfast = mealRepository.findByDailyDietAndMealType(dailyDiet.getId(), MealType.BREAKFAST).orElse(null);

                // then
                assertThat(findBreakfast).isNull();
            }
        }
    }
    @DisplayName("meal id 로 meal 을 삭제할 수 있다.")
    @Test
    void deleteById() {
        // given
        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
        dailyDietRepository.insert(dailyDiet);

        Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
        mealRepository.insert(breakfast);

        Meal lunch = createMeal(dailyDiet.getId(), MealType.LUNCH);
        mealRepository.insert(lunch);

        // when
        int deleteCount = mealRepository.deleteById(breakfast.getId());
        Meal findMeal = mealRepository.findById(breakfast.getId()).orElse(null);

        // then
        assertThat(deleteCount).isOne();
        assertThat(findMeal).isNull();

    }

    @Nested
    @DisplayName("findMealDetailById")
    class FindMealDetailById {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("meal id로 meal과 meal_food, food 정보를 JOIN하여 조회할 수 있다.")
            @Test
            void findMealDetailWithJoin() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                // when
                MealDetail mealDetail = mealRepository.findMealDetailById(breakfast.getId()).orElse(null);

                // then
                assertThat(mealDetail).isNotNull();
                assertThat(mealDetail.getId()).isEqualTo(breakfast.getId());
                assertThat(mealDetail.getType()).isEqualTo(MealType.BREAKFAST);
                assertThat(mealDetail.getDailyDietId()).isEqualTo(dailyDiet.getId());
                assertThat(mealDetail.getMealFoods()).isEmpty();
            }

            @DisplayName("meal에 여러 meal_food가 있을 경우 모두 조회된다.")
            @Test
            void findMealDetailWithMultipleMealFoods() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "description");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                Food food1 = createDummyFoodByName("닭가슴살");
                Food food2 = createDummyFoodByName("치킨");

                foodRepository.insert(food1);
                foodRepository.insert(food2);

                MealFood mealFood1 = createMealFood(breakfast.getId(), food1.getId(), 100.0);
                MealFood mealFood2 = createMealFood(breakfast.getId(), food2.getId(), 200.0);

                mealFoodRepository.batchInsert(List.of(mealFood1, mealFood2));

                // when
                MealDetail mealDetail = mealRepository.findMealDetailById(breakfast.getId()).orElse(null);

                // then
                assertThat(mealDetail).isNotNull();
                assertThat(mealDetail.getMealFoods()).hasSize(2)
                        .extracting(MealFoodDetail::getId)
                        .containsExactlyInAnyOrder(mealFood1.getId(), mealFood2.getId());
            }

            @DisplayName("존재하지 않는 meal id로 조회 시 빈 Optional을 반환한다.")
            @Test
            void returnEmptyOptionalWhenMealNotExists() {
                // given
                Long notExistingMealId = 99999L;

                // when
                MealDetail mealDetail = mealRepository.findMealDetailById(notExistingMealId).orElse(null);

                // then
                assertThat(mealDetail).isNull();
            }
        }
    }
}
