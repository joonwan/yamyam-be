package com.ssafy.yamyam_coach.service.daily_diet;

import com.ssafy.exception.daily_diet.DailyDietException;
import com.ssafy.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.food.FoodRepository;
import com.ssafy.yamyam_coach.repository.meal.MealRepository;
import com.ssafy.yamyam_coach.repository.mealfood.MealFoodRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import com.ssafy.yamyam_coach.service.daily_diet.request.DailyDietDetailServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.request.DailyDietUpdateDateServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.request.DailyDietUpdateDescriptionServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.request.RegisterDailyDietServiceRequest;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietDetailResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietsResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.MealFoodDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static com.ssafy.yamyam_coach.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DailyDietServiceTest extends IntegrationTestSupport {

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

    @Autowired
    MealFoodRepository mealFoodRepository;

    @Autowired
    DailyDietService dailyDietService;

    @Nested
    @DisplayName("RegisterDailyDiet")
    class RegisterDailyDiet {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("diet plan 이 존재하고 요청자의 diet plan 이며 요청된 날이 diet plan 기간에 속한다면 daily diet 를 생성할 수 있다.")
            @ParameterizedTest
            @CsvSource({
                    "2025-12-01", "2025-12-02", "2025-12-03"
            })
            void registerDailyDietPlan(String dateStr) {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(2);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                // when
                LocalDate date = LocalDate.parse(dateStr);

                RegisterDailyDietServiceRequest request = RegisterDailyDietServiceRequest.builder()
                        .description("description")
                        .dietPlanId(dietPlan.getId())
                        .date(date)
                        .build();

                Long registeredDailyDietId = dailyDietService.registerDailyDiet(user.getId(), request);
                DailyDiet findDailyDiet = dailyDietRepository.findById(registeredDailyDietId).orElse(null);

                // then
                assertThat(findDailyDiet).isNotNull();
                assertThat(findDailyDiet.getDescription()).isEqualTo(request.getDescription());
                assertThat(findDailyDiet.getDietPlanId()).isEqualTo(request.getDietPlanId());
                assertThat(findDailyDiet.getDate()).isEqualTo(date);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("diet plan 이 없을 경우 NOT_FOUND_DIET_PLAN 예외가 발생한다.")
            @Test
            void notFoundDietPlan() {
                // given

                User user = createDummyUser();
                userRepository.save(user);

                Long notExistDietPlanId = 99999L;

                RegisterDailyDietServiceRequest request = RegisterDailyDietServiceRequest.builder()
                        .description("description")
                        .dietPlanId(notExistDietPlanId)
                        .date(LocalDate.now())
                        .build();

                // when // then
                assertThatThrownBy(() -> dailyDietService.registerDailyDiet(user.getId(), request))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 조회할 수 없습니다.");
            }

            @DisplayName("date 가 diet plan 의 시작일과 종료일 사이에 없을 경우 INVALID_DATE 예외가 발생한다.")
            @ParameterizedTest
            @CsvSource({
                    "2025-11-30", "2025-12-04"
            })
            void invalidDate(String dateStr) {
                // given
                LocalDate date = LocalDate.parse(dateStr);

                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(2);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                // when
                RegisterDailyDietServiceRequest request = RegisterDailyDietServiceRequest.builder()
                        .description("description")
                        .dietPlanId(dietPlan.getId())
                        .date(date)
                        .build();

                // when // then
                assertThatThrownBy(() -> dailyDietService.registerDailyDiet(user.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("날짜가 식단 계획 기간을 벗어났습니다.");
            }

            @DisplayName("이미 daily diet 가 있는 날짜에 새로 만들려고 시도할 경우 예외가 발생한다.")
            @Test
            void duplicatedDate() {

                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(2);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                RegisterDailyDietServiceRequest request = RegisterDailyDietServiceRequest.builder()
                        .description("description")
                        .dietPlanId(dietPlan.getId())
                        .date(startDate)
                        .build();

                dailyDietService.registerDailyDiet(user.getId(), request);

                assertThatThrownBy(() -> dailyDietService.registerDailyDiet(user.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("이미 해당 날짜의 식단이 존재합니다.");
            }

            @DisplayName("사용자의 diet plan 이 아닌 경우 UNAUTHORIZED 예외가 발생한다.")
            @Test
            void unauthorizedDietPlan() {

                // given
                User user = createUser("name1", "nickname1", "email1@test.com", "password");
                userRepository.save(user);

                User other = createUser("name2", "nickname2", "email2@test.com", "password");
                userRepository.save(other);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(2);
                DietPlan dietPlan = createDietPlan(other.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                RegisterDailyDietServiceRequest request = RegisterDailyDietServiceRequest.builder()
                        .description("description")
                        .dietPlanId(dietPlan.getId())
                        .date(startDate)
                        .build();

                assertThatThrownBy(() -> dailyDietService.registerDailyDiet(user.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("일일 식단 제어 권한이 없습니다.");
            }

        }
    }

    @Nested
    @DisplayName("GetDailyDietByDietPlan")
    class GetDailyDietByDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("Diet plan 과 연결된 모든 daily diet 를 조회할 수 있다.")
            @Test
            void getDailyDietByDietPlan() {
                //given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(2);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet1 = createDailyDiet(dietPlan.getId(), startDate, "description 1");
                DailyDiet dailyDiet2 = createDailyDiet(dietPlan.getId(), startDate.plusDays(1), "description 2");

                dailyDietRepository.insert(dailyDiet1);
                dailyDietRepository.insert(dailyDiet2);

                // when
                DailyDietsResponse response = dailyDietService.getDailyDietByDietPlan(dietPlan.getId());
                List<DailyDietResponse> dailyDiets = response.getDailyDiets();

                // then
                assertThat(dailyDiets).isNotNull();
                assertThat(dailyDiets).hasSize(2)
                        .extracting(DailyDietResponse::getDate)
                        .containsExactly(startDate, startDate.plusDays(1))
                        .isSortedAccordingTo(LocalDate::compareTo);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("Diet plan 이 없을 경우 NOT_FOUND_DIET_PLAN 이 발생한다.")
            @Test
            void dietPlanNotFound() {
                // given
                Long notExistsDietPlanId = 99999L;

                // when // then
                assertThatThrownBy(() -> dailyDietService.getDailyDietByDietPlan(notExistsDietPlanId))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 조회할 수 없습니다.");

            }
        }
    }

    @Nested
    @DisplayName("GetDailyDietDetailByDietPlan")
    class GetDailyDietDetailByDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("모든 식사 타입(아침/점심/저녁/간식)이 있는 경우 DailyDiet 상세 정보를 조회할 수 있다.")
            @Test
            void getDailyDietDetailWithAllMealTypes() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 13);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "고단백 식단");
                String dayOfWeek = startDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);

                dailyDietRepository.insert(dailyDiet);

                // 음식 생성
                Food food1 = createDummyFoodByName("닭가슴살");
                Food food2 = createDummyFoodByName("현미밥");
                Food food3 = createDummyFoodByName("연어");
                Food food4 = createDummyFoodByName("견과류");
                foodRepository.insert(food1);
                foodRepository.insert(food2);
                foodRepository.insert(food3);
                foodRepository.insert(food4);

                // 아침 식사 생성
                Meal breakfast = createMeal(dailyDiet.getId(), com.ssafy.yamyam_coach.domain.meals.MealType.BREAKFAST);
                mealRepository.insert(breakfast);
                MealFood breakfastFood = createMealFood(breakfast.getId(), food1.getId(), 100.0);
                mealFoodRepository.insert(breakfastFood);

                // 점심 식사 생성
                Meal lunch = createMeal(dailyDiet.getId(), com.ssafy.yamyam_coach.domain.meals.MealType.LUNCH);
                mealRepository.insert(lunch);
                MealFood lunchFood = createMealFood(lunch.getId(), food2.getId(), 200.0);
                mealFoodRepository.insert(lunchFood);

                // 저녁 식사 생성
                Meal dinner = createMeal(dailyDiet.getId(), com.ssafy.yamyam_coach.domain.meals.MealType.DINNER);
                mealRepository.insert(dinner);
                MealFood dinnerFood = createMealFood(dinner.getId(), food3.getId(), 150.0);
                mealFoodRepository.insert(dinnerFood);

                // 간식 생성
                Meal snack = createMeal(dailyDiet.getId(), com.ssafy.yamyam_coach.domain.meals.MealType.SNACK);
                mealRepository.insert(snack);
                MealFood snackFood = createMealFood(snack.getId(), food4.getId(), 50.0);
                mealFoodRepository.insert(snackFood);

                // when
                DailyDietDetailServiceRequest request = new DailyDietDetailServiceRequest();
                request.setDietPlanId(dietPlan.getId());
                request.setDate(startDate);

                DailyDietDetailResponse response = dailyDietService.getDailyDietDetailByDietPlan(request);

                // then
                assertThat(response).isNotNull();
                assertThat(response.getDailyDietId()).isEqualTo(dailyDiet.getId());
                assertThat(response.getDate()).isEqualTo(startDate);
                assertThat(response.getDayOfWeek()).isEqualTo(dayOfWeek);
                assertThat(response.getDescription()).isEqualTo("고단백 식단");


                assertThat(response.getBreakfast()).hasSize(1);
                assertThat(response.getLunch()).hasSize(1);
                assertThat(response.getDinner()).hasSize(1);
                assertThat(response.getSnack()).hasSize(1);
            }

            @DisplayName("일부 식사만 있는 경우에도 정상적으로 조회되며 Null Pointer Exception이 발생하지 않는다.")
            @Test
            void getDailyDietDetailWithPartialMeals() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 13);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "간단한 식단");
                dailyDietRepository.insert(dailyDiet);

                // 음식 생성
                Food food1 = createDummyFoodByName("닭가슴살");
                Food food2 = createDummyFoodByName("현미밥");
                foodRepository.insert(food1);
                foodRepository.insert(food2);

                // 아침과 점심만 생성 (저녁, 간식 없음)
                Meal breakfast = createMeal(dailyDiet.getId(), com.ssafy.yamyam_coach.domain.meals.MealType.BREAKFAST);
                mealRepository.insert(breakfast);
                MealFood breakfastFood = createMealFood(breakfast.getId(), food1.getId(), 100.0);
                mealFoodRepository.insert(breakfastFood);

                Meal lunch = createMeal(dailyDiet.getId(), com.ssafy.yamyam_coach.domain.meals.MealType.LUNCH);
                mealRepository.insert(lunch);
                MealFood lunchFood = createMealFood(lunch.getId(), food2.getId(), 200.0);
                mealFoodRepository.insert(lunchFood);

                // when
                DailyDietDetailServiceRequest request = new DailyDietDetailServiceRequest();
                request.setDietPlanId(dietPlan.getId());
                request.setDate(startDate);

                DailyDietDetailResponse response = dailyDietService.getDailyDietDetailByDietPlan(request);

                // then
                assertThat(response).isNotNull();
                assertThat(response.getBreakfast()).hasSize(1);
                assertThat(response.getLunch()).hasSize(1);
                assertThat(response.getDinner()).isEmpty(); // 빈 리스트
                assertThat(response.getSnack()).isEmpty(); // 빈 리스트
            }

            @DisplayName("요일 정보가 올바르게 한글로 포함된다.")
            @ParameterizedTest
            @CsvSource({
                    "2025-12-15, 월요일",
                    "2025-12-16, 화요일",
                    "2025-12-17, 수요일",
                    "2025-12-18, 목요일",
                    "2025-12-19, 금요일",
                    "2025-12-20, 토요일",
                    "2025-12-21, 일요일"
            })
            void getDailyDietDetailWithCorrectDayOfWeek(String dateStr, String expectedDayOfWeek) {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = LocalDate.of(2025, 12, 31);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                LocalDate date = LocalDate.parse(dateStr);
                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), date, "테스트 식단");
                dailyDietRepository.insert(dailyDiet);

                // when
                DailyDietDetailServiceRequest request = new DailyDietDetailServiceRequest();
                request.setDietPlanId(dietPlan.getId());
                request.setDate(date);

                DailyDietDetailResponse response = dailyDietService.getDailyDietDetailByDietPlan(request);

                // then
                assertThat(response.getDayOfWeek()).isEqualTo(expectedDayOfWeek);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 DailyDiet를 조회하면 NOT_FOUND_DAILY_DIET 예외가 발생한다.")
            @Test
            void dailyDietNotFound() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                LocalDate nonExistentDate = LocalDate.of(2025, 12, 5);

                // when
                DailyDietDetailServiceRequest request = new DailyDietDetailServiceRequest();
                request.setDietPlanId(dietPlan.getId());
                request.setDate(nonExistentDate);

                // then
                assertThatThrownBy(() -> dailyDietService.getDailyDietDetailByDietPlan(request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("해당 일일 식단을 조회할 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("UpdateDescription")
    class UpdateDescription {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("DailyDiet의 description을 성공적으로 업데이트할 수 있다.")
            @Test
            void updateDescription() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "기존 설명");
                dailyDietRepository.insert(dailyDiet);

                // when
                DailyDietUpdateDescriptionServiceRequest request = DailyDietUpdateDescriptionServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .newDescription("새로운 설명")
                        .build();

                dailyDietService.updateDescription(user.getId(), request);

                DailyDiet updatedDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);

                // then
                assertThat(updatedDailyDiet).isNotNull();
                assertThat(updatedDailyDiet.getDescription()).isEqualTo("새로운 설명");
            }

            @DisplayName("동일한 description으로 업데이트 시도 시 변경되지 않는다.")
            @Test
            void updateDescriptionWithSameValue() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "기존 설명");
                dailyDietRepository.insert(dailyDiet);

                // when
                DailyDietUpdateDescriptionServiceRequest request = DailyDietUpdateDescriptionServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .newDescription("기존 설명")
                        .build();

                dailyDietService.updateDescription(user.getId(), request);

                DailyDiet updatedDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);

                // then
                assertThat(updatedDailyDiet).isNotNull();
                assertThat(updatedDailyDiet.getDescription()).isEqualTo("기존 설명");
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 DailyDiet의 description을 업데이트하려 하면 NOT_FOUND_DAILY_DIET 예외가 발생한다.")
            @Test
            void dailyDietNotFound() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistDailyDietId = 99999L;

                // when
                DailyDietUpdateDescriptionServiceRequest request = DailyDietUpdateDescriptionServiceRequest.builder()
                        .dailyDietId(notExistDailyDietId)
                        .newDescription("새로운 설명")
                        .build();

                // then
                assertThatThrownBy(() -> dailyDietService.updateDescription(user.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("해당 일일 식단을 조회할 수 없습니다.");
            }

            @DisplayName("다른 사용자의 DailyDiet description을 수정하려 하면 UNAUTHORIZED 예외가 발생한다.")
            @Test
            void unauthorizedUser() {
                // given
                User owner = createUser("소유자", "owner", "owner@test.com", "password");
                userRepository.save(owner);

                User other = createUser("다른사람", "other", "other@test.com", "password");
                userRepository.save(other);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(owner.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "기존 설명");
                dailyDietRepository.insert(dailyDiet);

                // when
                DailyDietUpdateDescriptionServiceRequest request = DailyDietUpdateDescriptionServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .newDescription("새로운 설명")
                        .build();

                // then
                assertThatThrownBy(() -> dailyDietService.updateDescription(other.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("일일 식단 제어 권한이 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("UpdateDate")
    class UpdateDate {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("DailyDiet의 날짜를 성공적으로 업데이트할 수 있다.")
            @Test
            void updateDate() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "식단 설명");
                dailyDietRepository.insert(dailyDiet);

                LocalDate newDate = startDate.plusDays(2);

                // when
                DailyDietUpdateDateServiceRequest request = DailyDietUpdateDateServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .newDate(newDate)
                        .build();

                dailyDietService.updateDate(user.getId(), request);

                DailyDiet updatedDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);

                // then
                assertThat(updatedDailyDiet).isNotNull();
                assertThat(updatedDailyDiet.getDate()).isEqualTo(newDate);
            }

            @DisplayName("동일한 날짜로 업데이트 시도 시 변경되지 않는다.")
            @Test
            void updateDateWithSameValue() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                LocalDate sameDate = startDate;
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "식단 설명");
                dailyDietRepository.insert(dailyDiet);

                // when
                DailyDietUpdateDateServiceRequest request = DailyDietUpdateDateServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .newDate(sameDate)
                        .build();

                dailyDietService.updateDate(user.getId(), request);

                DailyDiet updatedDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);

                // then
                assertThat(updatedDailyDiet).isNotNull();
                assertThat(updatedDailyDiet.getDate()).isEqualTo(sameDate);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 DailyDiet의 날짜를 업데이트하려 하면 NOT_FOUND_DAILY_DIET 예외가 발생한다.")
            @Test
            void dailyDietNotFound() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistDailyDietId = 99999L;

                // when
                DailyDietUpdateDateServiceRequest request = DailyDietUpdateDateServiceRequest.builder()
                        .dailyDietId(notExistDailyDietId)
                        .newDate(LocalDate.of(2025, 12, 1))
                        .build();

                // then
                assertThatThrownBy(() -> dailyDietService.updateDate(user.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("해당 일일 식단을 조회할 수 없습니다.");
            }

            @DisplayName("새 날짜가 DietPlan 기간을 벗어나면 INVALID_DATE 예외가 발생한다.")
            @ParameterizedTest
            @CsvSource({
                    "2025-11-30",
                    "2025-12-10"
            })
            void invalidDate(String dateStr) {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7); // 2025-12-08
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "식단 설명");
                dailyDietRepository.insert(dailyDiet);

                LocalDate invalidDate = LocalDate.parse(dateStr);

                // when
                DailyDietUpdateDateServiceRequest request = DailyDietUpdateDateServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .newDate(invalidDate)
                        .build();

                // then
                assertThatThrownBy(() -> dailyDietService.updateDate(user.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("날짜가 식단 계획 기간을 벗어났습니다.");
            }

            @DisplayName("새 날짜에 이미 DailyDiet가 존재하면 DUPLICATED_DATE 예외가 발생한다.")
            @Test
            void duplicatedDate() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                // 첫 번째 DailyDiet
                DailyDiet dailyDiet1 = createDailyDiet(dietPlan.getId(), startDate, "식단 1");
                dailyDietRepository.insert(dailyDiet1);

                // 두 번째 DailyDiet (다른 날짜)
                LocalDate anotherDate = startDate.plusDays(2);
                DailyDiet dailyDiet2 = createDailyDiet(dietPlan.getId(), anotherDate, "식단 2");
                dailyDietRepository.insert(dailyDiet2);

                // when - dailyDiet2의 날짜를 dailyDiet1과 동일하게 변경 시도
                DailyDietUpdateDateServiceRequest request = DailyDietUpdateDateServiceRequest.builder()
                        .dailyDietId(dailyDiet2.getId())
                        .newDate(dailyDiet1.getDate())
                        .build();

                // then
                assertThatThrownBy(() -> dailyDietService.updateDate(user.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("이미 해당 날짜의 식단이 존재합니다.");
            }

            @DisplayName("다른 사용자의 DailyDiet 날짜를 수정하려 하면 UNAUTHORIZED 예외가 발생한다.")
            @Test
            void unauthorizedUser() {
                // given
                User owner = createUser("user", "user nickname", "user@test.com", "password");
                userRepository.save(owner);

                User other = createUser("other", "other nickname", "other@test.com", "password");
                userRepository.save(other);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(owner.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "식단 설명");
                dailyDietRepository.insert(dailyDiet);

                LocalDate newDate = startDate.plusDays(2);

                // when
                DailyDietUpdateDateServiceRequest request = DailyDietUpdateDateServiceRequest.builder()
                        .dailyDietId(dailyDiet.getId())
                        .newDate(newDate)
                        .build();

                // then
                assertThatThrownBy(() -> dailyDietService.updateDate(other.getId(), request))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("일일 식단 제어 권한이 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("DeleteDailyDiet")
    class DeleteDailyDiet {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("DailyDiet를 성공적으로 삭제할 수 있다.")
            @Test
            void deleteDailyDiet() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(user.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "식단 설명");
                dailyDietRepository.insert(dailyDiet);

                // when
                dailyDietService.deleteDailyDiet(user.getId(), dailyDiet.getId());

                DailyDiet deletedDailyDiet = dailyDietRepository.findById(dailyDiet.getId()).orElse(null);

                // then
                assertThat(deletedDailyDiet).isNull();
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 DailyDiet를 삭제하려 하면 NOT_FOUND_DAILY_DIET 예외가 발생한다.")
            @Test
            void dailyDietNotFound() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistDailyDietId = 99999L;

                // when // then
                assertThatThrownBy(() -> dailyDietService.deleteDailyDiet(user.getId(), notExistDailyDietId))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("해당 일일 식단을 조회할 수 없습니다.");
            }

            @DisplayName("다른 사용자의 DailyDiet를 삭제하려 하면 UNAUTHORIZED 예외가 발생한다.")
            @Test
            void unauthorizedUser() {
                // given
                User user = createUser("user", "user nickname", "owner@test.com", "password");
                userRepository.save(user);

                User other = createUser("other", "other nickname", "other@test.com", "password");
                userRepository.save(other);

                LocalDate startDate = LocalDate.of(2025, 12, 1);
                LocalDate endDate = startDate.plusDays(7);
                DietPlan dietPlan = createDietPlan(other.getId(), "title", "content", false, false, startDate, endDate);
                dietPlanRepository.insert(dietPlan);

                DailyDiet dailyDiet = createDailyDiet(dietPlan.getId(), startDate, "식단 설명");
                dailyDietRepository.insert(dailyDiet);

                // when // then
                assertThatThrownBy(() -> dailyDietService.deleteDailyDiet(user.getId(), dailyDiet.getId()))
                        .isInstanceOf(DailyDietException.class)
                        .hasMessage("일일 식단 제어 권한이 없습니다.");
            }
        }
    }
}