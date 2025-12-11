package com.ssafy.yamyam_coach.controller.daily_diet;

import com.ssafy.yamyam_coach.RestControllerTestSupport;
import com.ssafy.yamyam_coach.controller.daily_diet.request.DailyDietUpdateRequest;
import com.ssafy.yamyam_coach.controller.daily_diet.request.RegisterDailyDietRequest;
import com.ssafy.yamyam_coach.exception.daily_diet.DailyDietException;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.service.daily_diet.DailyDietService;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietDetailResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.ssafy.yamyam_coach.exception.daily_diet.ErrorCode.*;
import static org.hamcrest.Matchers.endsWith;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DailyDietController.class)
@AutoConfigureMockMvc(addFilters = false)
class DailyDietControllerTest extends RestControllerTestSupport {

    @MockitoBean
    DailyDietService dailyDietService;

    @Nested
    @DisplayName("registerDailyDiet")
    class RegisterDailyDiet {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 요청으로 일일 식단 생성 시 201 Created와 Location 헤더를 반환한다")
            void registerDailyDiet() throws Exception {
                // given
                RegisterDailyDietRequest request = RegisterDailyDietRequest.builder()
                        .dietPlanId(1L)
                        .description("description")
                        .date(LocalDate.now())
                        .build();

                // stubbing
                given(dailyDietService.registerDailyDiet(anyLong(), any()))
                        .willReturn(1L);

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andExpect(header().string("Location", endsWith("/api/diet-plans/1/daily-diets/1")))
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("daily diet 가 존재하지 않을 경우 404 응답이 반환된다.")
            @Test
            void notExistDailyDiet() throws Exception {
                // given
                RegisterDailyDietRequest request = RegisterDailyDietRequest.builder()
                        .dietPlanId(1L)
                        .description("description")
                        .date(LocalDate.now())
                        .build();

                // stubbing
                given(dailyDietService.registerDailyDiet(anyLong(), any()))
                        .willThrow(new DailyDietException(NOT_FOUND_DAILY_DIET));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets")
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

            @DisplayName("dietplan 이 요청자의 것이 아닐 경우 403 응답이 반환된다.")
            @Test
            void forbidden() throws Exception {
                // given
                RegisterDailyDietRequest request = RegisterDailyDietRequest.builder()
                        .dietPlanId(1L)
                        .description("description")
                        .date(LocalDate.now())
                        .build();

                // stubbing
                given(dailyDietService.registerDailyDiet(anyLong(), any()))
                        .willThrow(new DailyDietException(UNAUTHORIZED));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.reasonPhrase").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
                        .andExpect(jsonPath("$.message").value("일일 식단 제어 권한이 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("date 가 diet plan 의 기간에 속하지 않을 경우 400 응답이 반환된다.")
            @Test
            void invalidDate() throws Exception {
                // given
                RegisterDailyDietRequest request = RegisterDailyDietRequest.builder()
                        .dietPlanId(1L)
                        .description("description")
                        .date(LocalDate.now())
                        .build();

                // stubbing
                given(dailyDietService.registerDailyDiet(anyLong(), any()))
                        .willThrow(new DailyDietException(INVALID_DATE));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.reasonPhrase").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                        .andExpect(jsonPath("$.message").value("날짜가 식단 계획 기간을 벗어났습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("중복된 날짜로 생성 시도할 경우 409 응답이 반환된다.")
            @Test
            void duplicatedDate() throws Exception {
                // given
                RegisterDailyDietRequest request = RegisterDailyDietRequest.builder()
                        .dietPlanId(1L)
                        .description("description")
                        .date(LocalDate.now())
                        .build();

                // stubbing
                given(dailyDietService.registerDailyDiet(anyLong(), any()))
                        .willThrow(new DailyDietException(DUPLICATED_DATE));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                        .andExpect(jsonPath("$.reasonPhrase").value(HttpStatus.CONFLICT.getReasonPhrase()))
                        .andExpect(jsonPath("$.message").value("이미 해당 날짜의 식단이 존재합니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("dietPlanId가 null일 경우 400 응답이 반환된다.")
            @Test
            void dietPlanIdIsNull() throws Exception {
                // given
                RegisterDailyDietRequest request = RegisterDailyDietRequest.builder()
                        .dietPlanId(null)
                        .description("description")
                        .date(LocalDate.now())
                        .build();

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets")
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

            @DisplayName("date가 null일 경우 400 응답이 반환된다.")
            @Test
            void dateIsNull() throws Exception {
                // given
                RegisterDailyDietRequest request = RegisterDailyDietRequest.builder()
                        .dietPlanId(1L)
                        .description("description")
                        .date(null)
                        .build();

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("description이 빈 문자열일 경우 400 응답이 반환된다.")
            @Test
            void descriptionIsBlank() throws Exception {
                // given
                RegisterDailyDietRequest request = RegisterDailyDietRequest.builder()
                        .dietPlanId(1L)
                        .description("")
                        .date(LocalDate.now())
                        .build();

                // when then
                mockMvc.perform(
                                post("/api/diet-plans/1/daily-diets")
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
    @DisplayName("getDailyDietByDietPlan")
    class GetDailyDietByDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 dietPlanId로 조회 시 200 OK와 일일 식단 목록을 반환한다")
            void getDailyDietByDietPlan() throws Exception {
                // given
                Long dietPlanId = 1L;
                LocalDate date1 = LocalDate.of(2025, 12, 1);
                LocalDate date2 = LocalDate.of(2025, 12, 2);

                DailyDietResponse response1 = DailyDietResponse.builder()
                        .dailyDietId(1L)
                        .date(date1)
                        .dayOfWeek("일요일")
                        .description("식단 1")
                        .build();

                DailyDietResponse response2 = DailyDietResponse.builder()
                        .dailyDietId(2L)
                        .date(date2)
                        .dayOfWeek("월요일")
                        .description("식단 2")
                        .build();

                DailyDietsResponse dailyDietsResponse = DailyDietsResponse.builder()
                        .dailyDiets(List.of(response1, response2))
                        .build();

                // stubbing
                given(dailyDietService.getDailyDietByDietPlan(dietPlanId))
                        .willReturn(dailyDietsResponse);

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/{dietPlanId}/daily-diets", dietPlanId)
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.dailyDiets").isArray())
                        .andExpect(jsonPath("$.dailyDiets.length()").value(2))
                        .andExpect(jsonPath("$.dailyDiets[0].dailyDietId").value(1L))
                        .andExpect(jsonPath("$.dailyDiets[0].date").value("2025-12-01"))
                        .andExpect(jsonPath("$.dailyDiets[1].dailyDietId").value(2L));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 diet plan 조회 시 404 응답이 반환된다.")
            @Test
            void notFoundDietPlan() throws Exception {
                // given
                Long dietPlanId = 999L;

                // stubbing
                given(dailyDietService.getDailyDietByDietPlan(dietPlanId))
                        .willThrow(new DietPlanException(com.ssafy.yamyam_coach.exception.diet_plan.ErrorCode.NOT_FOUND_DIET_PLAN));

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/{dietPlanId}/daily-diets", dietPlanId)
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getDailyDietDetailByDietPlanAndDate")
    class GetDailyDietDetailByDietPlanAndDate {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 dietPlanId와 date로 조회 시 200 OK와 상세 정보를 반환한다")
            void getDailyDietDetail() throws Exception {
                // given
                Long dietPlanId = 1L;
                LocalDate date = LocalDate.of(2025, 12, 13);

                DailyDietDetailResponse response = DailyDietDetailResponse.builder()
                        .dailyDietId(1L)
                        .date(date)
                        .dayOfWeek("금요일")
                        .description("고단백 식단")
                        .breakfast(Collections.emptyList())
                        .lunch(Collections.emptyList())
                        .dinner(Collections.emptyList())
                        .snack(Collections.emptyList())
                        .build();

                // stubbing
                given(dailyDietService.getDailyDietDetailByDietPlan(any()))
                        .willReturn(response);

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/{dietPlanId}/daily-diets/{date}", dietPlanId, date)
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.dailyDietId").value(1L))
                        .andExpect(jsonPath("$.date").value("2025-12-13"))
                        .andExpect(jsonPath("$.dayOfWeek").value("금요일"))
                        .andExpect(jsonPath("$.description").value("고단백 식단"))
                        .andExpect(jsonPath("$.breakfast").isArray())
                        .andExpect(jsonPath("$.lunch").isArray())
                        .andExpect(jsonPath("$.dinner").isArray())
                        .andExpect(jsonPath("$.snack").isArray());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 daily diet 조회 시 404 응답이 반환된다.")
            @Test
            void notFoundDailyDiet() throws Exception {
                // given
                Long dietPlanId = 1L;
                LocalDate date = LocalDate.of(2025, 12, 13);

                // stubbing
                given(dailyDietService.getDailyDietDetailByDietPlan(any()))
                        .willThrow(new DailyDietException(NOT_FOUND_DAILY_DIET));

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/{dietPlanId}/daily-diets/{date}", dietPlanId, date)
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 일일 식단을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("updateDailyDiet")
    class UpdateDailyDiet {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 요청으로 수정 시 204 No Content를 반환한다")
            void updateDailyDiet() throws Exception {
                // given
                Long dailyDietId = 1L;
                DailyDietUpdateRequest request = new DailyDietUpdateRequest();
                request.setNewDescription("새로운 설명");
                request.setNewDate(LocalDate.of(2025, 12, 15));

                // stubbing
                doNothing().when(dailyDietService).updateDailyDiet(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/{dailyDietId}", dailyDietId)
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

            @DisplayName("존재하지 않는 daily diet 수정 시 404 응답이 반환된다.")
            @Test
            void notFoundDailyDiet() throws Exception {
                // given
                Long dailyDietId = 999L;
                DailyDietUpdateRequest request = new DailyDietUpdateRequest();
                request.setNewDescription("새로운 설명");

                // stubbing
                doThrow(new DailyDietException(NOT_FOUND_DAILY_DIET))
                        .when(dailyDietService).updateDailyDiet(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/{dailyDietId}", dailyDietId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 일일 식단을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("권한이 없는 사용자가 수정 시도 시 403 응답이 반환된다.")
            @Test
            void unauthorized() throws Exception {
                // given
                Long dailyDietId = 1L;
                DailyDietUpdateRequest request = new DailyDietUpdateRequest();
                request.setNewDescription("새로운 설명");

                // stubbing
                doThrow(new DailyDietException(UNAUTHORIZED))
                        .when(dailyDietService).updateDailyDiet(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/{dailyDietId}", dailyDietId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("일일 식단 제어 권한이 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("유효하지 않은 날짜로 수정 시 400 응답이 반환된다.")
            @Test
            void invalidDate() throws Exception {
                // given
                Long dailyDietId = 1L;
                DailyDietUpdateRequest request = new DailyDietUpdateRequest();
                request.setNewDate(LocalDate.of(2025, 1, 1));

                // stubbing
                doThrow(new DailyDietException(INVALID_DATE))
                        .when(dailyDietService).updateDailyDiet(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/{dailyDietId}", dailyDietId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.message").value("날짜가 식단 계획 기간을 벗어났습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("중복된 날짜로 수정 시 409 응답이 반환된다.")
            @Test
            void duplicatedDate() throws Exception {
                // given
                Long dailyDietId = 1L;
                DailyDietUpdateRequest request = new DailyDietUpdateRequest();
                request.setNewDate(LocalDate.of(2025, 12, 2));

                // stubbing
                doThrow(new DailyDietException(DUPLICATED_DATE))
                        .when(dailyDietService).updateDailyDiet(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/1/daily-diets/{dailyDietId}", dailyDietId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                        .andExpect(jsonPath("$.message").value("이미 해당 날짜의 식단이 존재합니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("deleteDailyDiet")
    class DeleteDailyDiet {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 dailyDietId로 삭제 시 204 No Content를 반환한다")
            void deleteDailyDiet() throws Exception {
                // given
                Long dailyDietId = 1L;

                // stubbing
                doNothing().when(dailyDietService).deleteDailyDiet(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/1/daily-diets/{dailyDietId}", dailyDietId)
                        )
                        .andDo(print())
                        .andExpect(status().isNoContent())
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 daily diet 삭제 시 404 응답이 반환된다.")
            @Test
            void notFoundDailyDiet() throws Exception {
                // given
                Long dailyDietId = 999L;

                // stubbing
                doThrow(new DailyDietException(NOT_FOUND_DAILY_DIET))
                        .when(dailyDietService).deleteDailyDiet(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/1/daily-diets/{dailyDietId}", dailyDietId)
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 일일 식단을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("권한이 없는 사용자가 삭제 시도 시 403 응답이 반환된다.")
            @Test
            void unauthorized() throws Exception {
                // given
                Long dailyDietId = 1L;

                // stubbing
                doThrow(new DailyDietException(UNAUTHORIZED))
                        .when(dailyDietService).deleteDailyDiet(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/1/daily-diets/{dailyDietId}", dailyDietId)
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("일일 식단 제어 권한이 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

}