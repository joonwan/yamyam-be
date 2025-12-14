package com.ssafy.yamyam_coach.controller.meal;

import com.ssafy.yamyam_coach.RestControllerTestSupport;
import com.ssafy.yamyam_coach.controller.meal.request.CreateMealFoodRequest;
import com.ssafy.yamyam_coach.controller.meal.request.CreateMealRequest;
import com.ssafy.yamyam_coach.controller.meal.request.UpdateMealFoodRequest;
import com.ssafy.yamyam_coach.controller.meal.request.UpdateMealRequest;
import com.ssafy.yamyam_coach.domain.food.BaseUnit;
import com.ssafy.yamyam_coach.domain.meals.MealType;
import com.ssafy.yamyam_coach.exception.daily_diet.DailyDietException;
import com.ssafy.yamyam_coach.exception.food.FoodException;
import com.ssafy.yamyam_coach.exception.meal.MealException;
import com.ssafy.yamyam_coach.service.meal.MealService;
import com.ssafy.yamyam_coach.service.meal.response.MealDetailResponse;
import com.ssafy.yamyam_coach.service.meal.response.MealFoodDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.ssafy.yamyam_coach.exception.daily_diet.DailyDietErrorCode.*;
import static com.ssafy.yamyam_coach.exception.meal.MealErrorCode.*;
import static com.ssafy.yamyam_coach.exception.meal.MealErrorCode.UNAUTHORIZED;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MealController.class)
@AutoConfigureMockMvc(addFilters = false)
class MealControllerTest extends RestControllerTestSupport {

    @MockitoBean
    MealService mealService;

    @Nested
    @DisplayName("registerMeal")
    class RegisterMeal {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 요청으로 meal 생성 시 201 Created와 Location 헤더를 반환한다")
            void registerMeal() throws Exception {
                // given
                CreateMealFoodRequest mealFood1 = CreateMealFoodRequest.builder()
                        .foodId(1L)
                        .amount(200.0)
                        .build();

                CreateMealFoodRequest mealFood2 = CreateMealFoodRequest.builder()
                        .foodId(2L)
                        .amount(150.0)
                        .build();

                CreateMealRequest request = CreateMealRequest.builder()
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of(mealFood1, mealFood2))
                        .build();

                // stubbing
                given(mealService.createMeal(anyLong(), any()))
                        .willReturn(1L);

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets/1/meals")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andExpect(header().string("Location", endsWith("/api/diet-plans/1/daily-diets/1/meals/1")))
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 dailyDiet으로 meal 생성 시 404 응답이 반환된다")
            @Test
            void notFoundDailyDiet() throws Exception {
                // given
                CreateMealRequest request = CreateMealRequest.builder()
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of())
                        .build();

                // stubbing
                given(mealService.createMeal(anyLong(), any()))
                        .willThrow(new DailyDietException(NOT_FOUND_DAILY_DIET));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets/1/meals")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.reasonPhrase").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                        .andExpect(jsonPath("$.message").value("해당 일일 식단을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("다른 사용자의 dietPlan에 meal 생성 시 403 응답이 반환된다")
            @Test
            void unauthorized() throws Exception {
                // given
                CreateMealRequest request = CreateMealRequest.builder()
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of())
                        .build();

                // stubbing
                given(mealService.createMeal(anyLong(), any()))
                        .willThrow(new MealException(UNAUTHORIZED));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets/1/meals")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.reasonPhrase").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
                        .andExpect(jsonPath("$.message").value("식사를 생성할 권한이 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("이미 같은 mealType이 존재하는 경우 409 응답이 반환된다")
            @Test
            void duplicatedMealType() throws Exception {
                // given
                CreateMealRequest request = CreateMealRequest.builder()
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of())
                        .build();

                // stubbing
                given(mealService.createMeal(anyLong(), any()))
                        .willThrow(new MealException(DUPLICATED_MEAL_TYPE));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets/1/meals")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                        .andExpect(jsonPath("$.reasonPhrase").value(HttpStatus.CONFLICT.getReasonPhrase()))
                        .andExpect(jsonPath("$.message").value("이미 해당 타입의 식사가 존재합니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("존재하지 않는 food로 meal 생성 시 404 응답이 반환된다")
            @Test
            void notFoundFood() throws Exception {
                // given
                CreateMealFoodRequest mealFood = CreateMealFoodRequest.builder()
                        .foodId(999L)
                        .amount(200.0)
                        .build();

                CreateMealRequest request = CreateMealRequest.builder()
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(List.of(mealFood))
                        .build();

                // stubbing
                given(mealService.createMeal(anyLong(), any()))
                        .willThrow(new FoodException(com.ssafy.yamyam_coach.exception.food.FoodErrorCode.NOT_FOUND_FOOD));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets/1/meals")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 음식을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("mealType이 null일 경우 400 응답이 반환된다")
            @Test
            void mealTypeIsNull() throws Exception {
                // given
                CreateMealRequest request = CreateMealRequest.builder()
                        .mealType(null)
                        .mealFoodRequests(List.of())
                        .build();

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets/1/meals")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.reasonPhrase").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("mealFoodRequests가 null일 경우 400 응답이 반환된다")
            @Test
            void mealFoodRequestsIsNull() throws Exception {
                // given
                CreateMealRequest request = CreateMealRequest.builder()
                        .mealType(MealType.BREAKFAST)
                        .mealFoodRequests(null)
                        .build();

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets/1/meals")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getMeal")
    class GetMeal {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 mealId로 조회 시 200 OK와 meal 상세 정보를 반환한다")
            void getMeal() throws Exception {
                // given
                Long mealId = 1L;

                MealFoodDetailResponse mealFood1 = MealFoodDetailResponse.builder()
                        .mealFoodId(1L)
                        .quantity(200.0)
                        .foodId(1L)
                        .foodName("닭가슴살")
                        .category("육류")
                        .baseUnit(BaseUnit.g)
                        .energyPer100(165.0)
                        .proteinPer100(31.0)
                        .fatPer100(3.6)
                        .carbohydratePer100(0.0)
                        .build();

                MealFoodDetailResponse mealFood2 = MealFoodDetailResponse.builder()
                        .mealFoodId(2L)
                        .quantity(150.0)
                        .foodId(2L)
                        .foodName("현미밥")
                        .category("곡류")
                        .baseUnit(BaseUnit.g)
                        .energyPer100(140.0)
                        .proteinPer100(2.6)
                        .fatPer100(0.9)
                        .carbohydratePer100(29.0)
                        .build();

                MealDetailResponse serviceResponse = MealDetailResponse.builder()
                        .mealId(mealId)
                        .mealType(MealType.BREAKFAST)
                        .dailyDietId(1L)
                        .mealFoods(List.of(mealFood1, mealFood2))
                        .build();

                // stubbing
                given(mealService.getMealById(eq(mealId)))
                        .willReturn(serviceResponse);

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mealId").value(mealId))
                        .andExpect(jsonPath("$.mealType").value("BREAKFAST"))
                        .andExpect(jsonPath("$.dailyDietId").value(1L))
                        .andExpect(jsonPath("$.mealFoods").isArray())
                        .andExpect(jsonPath("$.mealFoods.length()").value(2))
                        .andExpect(jsonPath("$.mealFoods[0].mealFoodId").value(1L))
                        .andExpect(jsonPath("$.mealFoods[0].foodName").value("닭가슴살"))
                        .andExpect(jsonPath("$.mealFoods[1].mealFoodId").value(2L))
                        .andExpect(jsonPath("$.mealFoods[1].foodName").value("현미밥"));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 meal 조회 시 404 응답이 반환된다")
            @Test
            void notFoundMeal() throws Exception {
                // given
                Long mealId = 999L;

                // stubbing
                given(mealService.getMealById(eq(mealId)))
                        .willThrow(new MealException(NOT_FOUND_MEAL));

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 식사를 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
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
            @DisplayName("유효한 요청으로 meal 수정 시 204 No Content를 반환한다")
            void updateMeal() throws Exception {
                // given
                Long mealId = 1L;

                UpdateMealFoodRequest mealFood = UpdateMealFoodRequest.builder()
                        .foodId(1L)
                        .amount(250.0)
                        .build();

                UpdateMealRequest request = UpdateMealRequest.builder()
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(List.of(mealFood))
                        .build();

                // stubbing
                doNothing().when(mealService).updateMeal(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNoContent())
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 meal 수정 시 404 응답이 반환된다")
            @Test
            void notFoundMeal() throws Exception {
                // given
                Long mealId = 999L;

                UpdateMealRequest request = UpdateMealRequest.builder()
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(List.of())
                        .build();

                // stubbing
                doThrow(new MealException(NOT_FOUND_MEAL))
                        .when(mealService).updateMeal(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 식사를 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("다른 사용자의 meal 수정 시 403 응답이 반환된다")
            @Test
            void unauthorized() throws Exception {
                // given
                Long mealId = 1L;

                UpdateMealRequest request = UpdateMealRequest.builder()
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(List.of())
                        .build();

                // stubbing
                doThrow(new MealException(UNAUTHORIZED))
                        .when(mealService).updateMeal(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("식사를 생성할 권한이 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("변경하려는 mealType이 이미 존재할 때 409 응답이 반환된다")
            @Test
            void duplicatedMealType() throws Exception {
                // given
                Long mealId = 1L;

                UpdateMealRequest request = UpdateMealRequest.builder()
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(List.of())
                        .build();

                // stubbing
                doThrow(new MealException(DUPLICATED_MEAL_TYPE))
                        .when(mealService).updateMeal(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                        .andExpect(jsonPath("$.message").value("이미 해당 타입의 식사가 존재합니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("존재하지 않는 food로 meal 수정 시 404 응답이 반환된다")
            @Test
            void notFoundFood() throws Exception {
                // given
                Long mealId = 1L;

                UpdateMealFoodRequest mealFood = UpdateMealFoodRequest.builder()
                        .foodId(999L)
                        .amount(250.0)
                        .build();

                UpdateMealRequest request = UpdateMealRequest.builder()
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(List.of(mealFood))
                        .build();

                // stubbing
                doThrow(new FoodException(com.ssafy.yamyam_coach.exception.food.FoodErrorCode.NOT_FOUND_FOOD))
                        .when(mealService).updateMeal(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 음식을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("mealType이 null일 경우 400 응답이 반환된다")
            @Test
            void mealTypeIsNull() throws Exception {
                // given
                Long mealId = 1L;

                UpdateMealRequest request = UpdateMealRequest.builder()
                        .mealType(null)
                        .mealFoodUpdateRequests(List.of())
                        .build();

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("mealFoodUpdateRequests가 null일 경우 400 응답이 반환된다")
            @Test
            void mealFoodUpdateRequestsIsNull() throws Exception {
                // given
                Long mealId = 1L;

                UpdateMealRequest request = UpdateMealRequest.builder()
                        .mealType(MealType.LUNCH)
                        .mealFoodUpdateRequests(null)
                        .build();

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
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
            @DisplayName("유효한 mealId로 삭제 시 204 No Content를 반환한다")
            void deleteMeal() throws Exception {
                // given
                Long mealId = 1L;

                // stubbing
                doNothing().when(mealService).deleteMeal(anyLong(), eq(mealId));

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                        )
                        .andDo(print())
                        .andExpect(status().isNoContent())
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 meal 삭제 시 404 응답이 반환된다")
            @Test
            void notFoundMeal() throws Exception {
                // given
                Long mealId = 999L;

                // stubbing
                doThrow(new MealException(NOT_FOUND_MEAL))
                        .when(mealService).deleteMeal(anyLong(), eq(mealId));

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 식사를 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("다른 사용자의 meal 삭제 시 403 응답이 반환된다")
            @Test
            void unauthorized() throws Exception {
                // given
                Long mealId = 1L;

                // stubbing
                doThrow(new MealException(UNAUTHORIZED))
                        .when(mealService).deleteMeal(anyLong(), eq(mealId));

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/1/daily-diets/1/meals/{mealId}", mealId)
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("식사를 생성할 권한이 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }
}
