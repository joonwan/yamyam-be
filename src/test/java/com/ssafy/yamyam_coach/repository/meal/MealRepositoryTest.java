package com.ssafy.yamyam_coach.repository.meal;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
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
}
