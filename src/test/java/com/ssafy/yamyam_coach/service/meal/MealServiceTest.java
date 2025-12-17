package com.ssafy.yamyam_coach.service.meal;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.exception.daily_diet.DailyDietException;
import com.ssafy.yamyam_coach.exception.food.FoodException;
import com.ssafy.yamyam_coach.exception.meal.MealException;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.food.FoodRepository;
import com.ssafy.yamyam_coach.repository.meal.MealRepository;
import com.ssafy.yamyam_coach.repository.meal.response.MealFoodDetail;
import com.ssafy.yamyam_coach.repository.mealfood.MealFoodRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import com.ssafy.yamyam_coach.service.meal.request.CreateMealFoodServiceRequest;
import com.ssafy.yamyam_coach.service.meal.request.CreateMealServiceRequest;
import com.ssafy.yamyam_coach.service.meal.request.UpdateMealFoodServiceRequest;
import com.ssafy.yamyam_coach.service.meal.request.UpdateMealServiceRequest;
import com.ssafy.yamyam_coach.service.meal.response.MealDetailResponse;
import com.ssafy.yamyam_coach.service.meal.response.MealFoodDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static com.ssafy.yamyam_coach.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

class MealServiceTest extends IntegrationTestSupport {

    @Autowired
    MealService mealService;

    @Autowired
    MealRepository mealRepository;

    @Autowired
    MealFoodRepository mealFoodRepository;

    @Autowired
    DailyDietRepository dailyDietRepository;

    @Autowired
    DietPlanRepository dietPlanRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FoodRepository foodRepository;

    @Nested
    @DisplayName("createMeal")
    class CreateMeal {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 요청으로 meal과 meal_food를 정상적으로 생성한다")
            void createMealSuccessfully() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                Food food1 = createDummyFoodByName("닭가슴살");
                Food food2 = createDummyFoodByName("현미밥");
                foodRepository.insert(food1);
                foodRepository.insert(food2);

                CreateMealFoodServiceRequest mealFood1 = CreateMealFoodServiceRequest.builder()
                        .foodId(food1.getId())
                        .amount(200.0)
                        .build();

                CreateMealFoodServiceRequest mealFood2 = CreateMealFoodServiceRequest.builder()
                        .foodId(food2.getId())
                        .amount(200.0)
                        .build();

                CreateMealServiceRequest request = CreateMealServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of(mealFood1, mealFood2))
                        .build();

                // when
                mealService.createMeal(user.getId(), request);

                // then
                Meal findMeal = mealRepository.findByDailyDietAndMealType(dailyDiet.getId(), MealType.BREAKFAST).orElse(null);
                assertThat(findMeal).isNotNull();
                List<MealFood> mealFoods = mealFoodRepository.findByMeal(findMeal.getId());
                assertThat(mealFoods).hasSize(2)
                        .extracting(MealFood::getFoodId)
                        .containsExactlyInAnyOrder(mealFood1.getFoodId(), mealFood2.getFoodId());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @Test
            @DisplayName("존재하지 않는 dailyDiet으로 meal 생성 시 NOT_FOUND_DAILY_DIET 예외가 발생한다")
            void createMealWithNotExistingDailyDiet() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistingDailyDietId = 99999L;

                CreateMealServiceRequest request = CreateMealServiceRequest.builder()
                        .dailyDietId(notExistingDailyDietId)
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of())
                        .build();

                // when & then
                assertThatThrownBy(() -> mealService.createMeal(user.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("해당 일일 식단을 조회할 수 없습니다.");
            }

            @Test
            @DisplayName("다른 사용자의 dietPlan에 meal 생성 시도 시 UNAUTHORIZED_FOR_DELETE 예외가 발생한다")
            void createMealWithOtherUsersDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                User otherUser = createUser("다른사람", "다른닉네임", "other@example.com", "password");
                userRepository.save(otherUser);

                DietPlan othersDietPlan = createDummyDietPlan(otherUser.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(othersDietPlan);

                DailyDiet othersDailyDiet = createDailyDiet(othersDietPlan.getId(), LocalDate.now(), "다른 사람의 식단");
                dailyDietRepository.insert(othersDailyDiet);

                CreateMealServiceRequest request = CreateMealServiceRequest.builder()
                        .dailyDietId(othersDailyDiet.getId())
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of())
                        .build();

                // when & then
                assertThatThrownBy(() -> mealService.createMeal(user.getId(), request))
                        .isInstanceOf(MealException.class)
                        .hasMessage("식사를 생성할 권한이 없습니다.");
            }

            @Test
            @DisplayName("이미 같은 mealType이 존재하는 dailyDiet에 meal 생성 시 DUPLICATED_MEAL_TYPE 예외가 발생한다")
            void createMealWithDuplicatedMealType() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                // 이미 아침 식사가 존재
                Meal existingBreakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(existingBreakfast);

                CreateMealServiceRequest request = CreateMealServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of())
                        .build();

                // when & then
                assertThatThrownBy(() -> mealService.createMeal(user.getId(), request))
                        .isInstanceOf(MealException.class)
                        .hasMessage("이미 해당 타입의 식사가 존재합니다.");
            }

            @Test
            @DisplayName("존재하지 않는 food로 meal 생성 시 NOT_FOUND_FOOD 예외가 발생한다")
            void createMealWithNotExistingFood() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                Long notExistingFoodId = 99999L;

                CreateMealFoodServiceRequest mealFood = CreateMealFoodServiceRequest.builder()
                        .foodId(notExistingFoodId)
                        .amount(200.0)
                        .build();

                CreateMealServiceRequest request = CreateMealServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of(mealFood))
                        .build();

                // when & then
                assertThatThrownBy(() -> mealService.createMeal(user.getId(), request))
                        .isInstanceOf(FoodException.class)
                        .hasMessage("해당 음식을 조회할 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("updateMeal")
    class UpdateMeal {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("meal type과 meal_food를 정상적으로 수정한다")
            void updateMealTypeAndMealFoods() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                Food oldFood = createDummyFoodByName("닭가슴살");
                foodRepository.insert(oldFood);

                MealFood oldMealFood = createMealFood(breakfast.getId(), oldFood.getId(), 100.0);
                mealFoodRepository.insert(oldMealFood);

                // 새로운 음식들
                Food newFood1 = createDummyFoodByName("연어");
                Food newFood2 = createDummyFoodByName("현미밥");
                foodRepository.insert(newFood1);
                foodRepository.insert(newFood2);

                UpdateMealFoodServiceRequest updateMealFood1 = UpdateMealFoodServiceRequest.builder()
                        .foodId(newFood1.getId())
                        .amount(150.0)
                        .build();

                UpdateMealFoodServiceRequest updateMealFood2 = UpdateMealFoodServiceRequest.builder()
                        .foodId(newFood2.getId())
                        .amount(200.0)
                        .build();

                UpdateMealServiceRequest request = UpdateMealServiceRequest.builder()
                        .mealId(breakfast.getId())
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(List.of(updateMealFood1, updateMealFood2))
                        .build();

                // when
                mealService.updateMeal(user.getId(), request);

                // then
                Meal updatedMeal = mealRepository.findById(breakfast.getId()).orElse(null);
                assertThat(updatedMeal).isNotNull();
                assertThat(updatedMeal.getType()).isEqualTo(MealType.LUNCH);

                List<MealFood> updatedMealFoods = mealFoodRepository.findByMeal(breakfast.getId());
                assertThat(updatedMealFoods).hasSize(2)
                        .extracting(MealFood::getFoodId)
                        .containsExactlyInAnyOrder(newFood1.getId(), newFood2.getId());
            }

            @Test
            @DisplayName("meal type은 그대로 두고 meal_food만 변경한다")
            void updateOnlyMealFoods() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                Food oldFood = createDummyFoodByName("닭가슴살");
                foodRepository.insert(oldFood);

                MealFood oldMealFood = createMealFood(breakfast.getId(), oldFood.getId(), 100.0);
                mealFoodRepository.insert(oldMealFood);

                Food newFood = createDummyFoodByName("연어");
                foodRepository.insert(newFood);

                UpdateMealFoodServiceRequest updateMealFood = UpdateMealFoodServiceRequest.builder()
                        .foodId(newFood.getId())
                        .amount(150.0)
                        .build();

                UpdateMealServiceRequest request = UpdateMealServiceRequest.builder()
                        .mealId(breakfast.getId())
                        .mealType(MealType.BREAKFAST)
                        .mealFoodUpdateRequests(List.of(updateMealFood))
                        .build();

                // when
                mealService.updateMeal(user.getId(), request);

                // then
                Meal updatedMeal = mealRepository.findById(breakfast.getId()).orElse(null);
                assertThat(updatedMeal).isNotNull();
                assertThat(updatedMeal.getType()).isEqualTo(MealType.BREAKFAST);

                List<MealFood> updatedMealFoods = mealFoodRepository.findByMeal(breakfast.getId());
                assertThat(updatedMealFoods).hasSize(1);
                assertThat(updatedMealFoods.get(0).getFoodId()).isEqualTo(newFood.getId());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @Test
            @DisplayName("존재하지 않는 meal을 수정하려 할 때 NOT_FOUND_MEAL 예외가 발생한다")
            void updateNotExistingMeal() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistingMealId = 99999L;

                UpdateMealServiceRequest request = UpdateMealServiceRequest.builder()
                        .mealId(notExistingMealId)
                        .mealType(MealType.BREAKFAST)
                        .mealFoodUpdateRequests(List.of())
                        .build();

                // when & then
                assertThatThrownBy(() -> mealService.updateMeal(user.getId(), request))
                        .isInstanceOf(MealException.class)
                        .hasMessage("해당 식사를 조회할 수 없습니다.");
            }

            @Test
            @DisplayName("다른 사용자의 meal을 수정하려 할 때 UNAUTHORIZED_FOR_DELETE 예외가 발생한다")
            void updateOtherUsersMeal() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                User otherUser = createUser("다른사람", "다른닉네임", "other@example.com", "password");
                userRepository.save(otherUser);

                DietPlan othersDietPlan = createDummyDietPlan(otherUser.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(othersDietPlan);

                DailyDiet othersDailyDiet = createDailyDiet(othersDietPlan.getId(), LocalDate.now(), "다른 사람의 식단");
                dailyDietRepository.insert(othersDailyDiet);

                Meal othersMeal = createMeal(othersDailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(othersMeal);

                UpdateMealServiceRequest request = UpdateMealServiceRequest.builder()
                        .mealId(othersMeal.getId())
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(List.of())
                        .build();

                // when & then
                assertThatThrownBy(() -> mealService.updateMeal(user.getId(), request))
                        .isInstanceOf(MealException.class)
                        .hasMessage("식사를 생성할 권한이 없습니다.");
            }

            @Test
            @DisplayName("변경하려는 meal type이 이미 존재할 때 DUPLICATED_MEAL_TYPE 예외가 발생한다")
            void updateMealTypeToExistingType() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                // 이미 점심 식사가 존재
                Meal lunch = createMeal(dailyDiet.getId(), MealType.LUNCH);
                mealRepository.insert(lunch);

                UpdateMealServiceRequest request = UpdateMealServiceRequest.builder()
                        .mealId(breakfast.getId())
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(List.of())
                        .build();

                // when & then
                assertThatThrownBy(() -> mealService.updateMeal(user.getId(), request))
                        .isInstanceOf(MealException.class)
                        .hasMessage("이미 해당 타입의 식사가 존재합니다.");
            }

            @Test
            @DisplayName("존재하지 않는 food로 meal 수정 시 NOT_FOUND_FOOD 예외가 발생한다")
            void updateMealWithNotExistingFood() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                Long notExistingFoodId = 99999L;


                UpdateMealFoodServiceRequest updatedMealFood = UpdateMealFoodServiceRequest.builder()
                        .foodId(notExistingFoodId)
                        .amount(200.0)
                        .build();

                UpdateMealServiceRequest request = UpdateMealServiceRequest.builder()
                        .mealId(breakfast.getId())
                        .mealType(MealType.BREAKFAST)
                        .mealFoodUpdateRequests(List.of(updatedMealFood))
                        .build();

                // when & then
                assertThatThrownBy(() -> mealService.updateMeal(user.getId(), request))
                        .isInstanceOf(FoodException.class)
                        .hasMessage("해당 음식을 조회할 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("deleteMeal")
    class DeleteMeal {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("meal을 정상적으로 삭제한다 (meal_food도 cascade로 삭제)")
            void deleteMealSuccessfully() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                Food food = createDummyFoodByName("닭가슴살");
                foodRepository.insert(food);

                MealFood mealFood = createMealFood(breakfast.getId(), food.getId(), 100.0);
                mealFoodRepository.insert(mealFood);

                // when
                mealService.deleteMeal(user.getId(), breakfast.getId());

                // then
                Meal deletedMeal = mealRepository.findById(breakfast.getId()).orElse(null);
                assertThat(deletedMeal).isNull();

                // cascade로 meal_food도 삭제되었는지 확인
                List<MealFood> mealFoods = mealFoodRepository.findByMeal(breakfast.getId());
                assertThat(mealFoods).isEmpty();
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @Test
            @DisplayName("존재하지 않는 meal을 삭제하려 할 때 NOT_FOUND_MEAL 예외가 발생한다")
            void deleteNotExistingMeal() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistingMealId = 99999L;

                // when & then
                assertThatThrownBy(() -> mealService.deleteMeal(user.getId(), notExistingMealId))
                        .isInstanceOf(MealException.class)
                        .hasMessage("해당 식사를 조회할 수 없습니다.");
            }

            @Test
            @DisplayName("다른 사용자의 meal을 삭제하려 할 때 UNAUTHORIZED_FOR_DELETE 예외가 발생한다")
            void deleteOtherUsersMeal() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                User otherUser = createUser("다른사람", "다른닉네임", "other@example.com", "password");
                userRepository.save(otherUser);

                DietPlan othersDietPlan = createDummyDietPlan(otherUser.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(othersDietPlan);

                DailyDiet othersDailyDiet = createDailyDiet(othersDietPlan.getId(), LocalDate.now(), "다른 사람의 식단");
                dailyDietRepository.insert(othersDailyDiet);

                Meal othersMeal = createMeal(othersDailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(othersMeal);

                // when & then
                assertThatThrownBy(() -> mealService.deleteMeal(user.getId(), othersMeal.getId()))
                        .isInstanceOf(MealException.class)
                        .hasMessage("식사를 생성할 권한이 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("getMealById")
    class GetMealById {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("식사에 대한 상세 조회를 할 수 있다.")
            @Test
            void getMealById() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), LocalDate.now(), "오늘의 식단");
                dailyDietRepository.insert(dailyDiet);

                Meal breakfast = createMeal(dailyDiet.getId(), MealType.BREAKFAST);
                mealRepository.insert(breakfast);

                Food food1 = createDummyFoodByName("닭가슴살");
                foodRepository.insert(food1);

                Food food2 = createDummyFoodByName("닭도리탕");
                foodRepository.insert(food2);

                MealFood mealFood1 = createMealFood(breakfast.getId(), food1.getId(), 100.0);
                MealFood mealFood2 = createMealFood(breakfast.getId(), food2.getId(), 100.0);
                mealFoodRepository.insert(mealFood1);
                mealFoodRepository.insert(mealFood2);

                // when
                MealDetailResponse response = mealService.getMealById(breakfast.getId());

                // then
                assertThat(response).isNotNull();
                assertThat(response.getMealId()).isEqualTo(breakfast.getId());
                assertThat(response.getMealType()).isEqualTo(breakfast.getType());
                assertThat(response.getDailyDietId()).isEqualTo(dailyDiet.getId());
                assertThat(response.getMealFoods()).hasSize(2)
                        .extracting(MealFoodDetailResponse::getMealFoodId)
                        .containsExactlyInAnyOrder(mealFood1.getId(), mealFood2.getId());

            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("식사가 없을 경우 NOT_FOUND_MEAL 예외가 발생한다.")
            @Test
            void test2() {
                // given

                // when // then
                assertThatThrownBy(() -> mealService.getMealById(99999L))
                        .isInstanceOf(MealException.class)
                        .hasMessage("해당 식사를 조회할 수 없습니다.");

            }
        }
    }
}
