package com.ssafy.yamyam_coach.controller.diet_plan;

import com.ssafy.yamyam_coach.RestControllerTestSupport;
import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateDietPlanRequest;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.service.diet_plan.DietPlanService;
import com.ssafy.yamyam_coach.service.diet_plan.response.DietPlanServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.ssafy.yamyam_coach.exception.diet_plan.ErrorCode.*;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DietPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class DietPlanControllerTest extends RestControllerTestSupport {

    @MockitoBean
    DietPlanService dietPlanService;

    @Nested
    @DisplayName("registerDietPlan")
    class RegisterDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 요청으로 식단 계획 생성 시 201 Created와 Location 헤더를 반환한다")
            void registerDietPlan() throws Exception {
                // given
                CreateDietPlanRequest request = new CreateDietPlanRequest();
                request.setTitle("다이어트 식단");
                request.setContent("건강한 다이어트를 위한 식단");
                request.setStartDate(LocalDate.of(2025, 12, 1));
                request.setEndDate(LocalDate.of(2025, 12, 31));

                // stubbing
                given(dietPlanService.registerDietPlan(anyLong(), any()))
                        .willReturn(1L);

                // when then
                mockMvc.perform(
                                post("/api/diet-plans")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andExpect(header().string("Location", endsWith("/api/diet-plans/1")))
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("title이 빈 문자열일 경우 400 응답이 반환된다.")
            @Test
            void titleIsBlank() throws Exception {
                // given
                CreateDietPlanRequest request = new CreateDietPlanRequest();
                request.setTitle("");
                request.setContent("내용");
                request.setStartDate(LocalDate.of(2025, 12, 1));
                request.setEndDate(LocalDate.of(2025, 12, 31));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("content가 빈 문자열일 경우 400 응답이 반환된다.")
            @Test
            void contentIsBlank() throws Exception {
                // given
                CreateDietPlanRequest request = new CreateDietPlanRequest();
                request.setTitle("제목");
                request.setContent("");
                request.setStartDate(LocalDate.of(2025, 12, 1));
                request.setEndDate(LocalDate.of(2025, 12, 31));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("startDate가 null일 경우 400 응답이 반환된다.")
            @Test
            void startDateIsNull() throws Exception {
                // given
                CreateDietPlanRequest request = new CreateDietPlanRequest();
                request.setTitle("제목");
                request.setContent("내용");
                request.setStartDate(null);
                request.setEndDate(LocalDate.of(2025, 12, 31));

                // when then
                mockMvc.perform(
                                post("/api/diet-plans")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("endDate가 null일 경우 400 응답이 반환된다.")
            @Test
            void endDateIsNull() throws Exception {
                // given
                CreateDietPlanRequest request = new CreateDietPlanRequest();
                request.setTitle("제목");
                request.setContent("내용");
                request.setStartDate(LocalDate.of(2025, 12, 1));
                request.setEndDate(null);

                // when then
                mockMvc.perform(
                                post("/api/diet-plans")
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
    @DisplayName("getDietPlanById")
    class GetDietPlanById {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 dietPlanId로 조회 시 200 OK와 식단 계획 정보를 반환한다")
            void getDietPlanById() throws Exception {
                // given
                Long dietPlanId = 1L;
                DietPlanServiceResponse response = DietPlanServiceResponse.builder()
                        .dietPlanId(dietPlanId)
                        .title("다이어트 식단")
                        .content("건강한 다이어트를 위한 식단")
                        .isPrimary(true)
                        .startDate(LocalDate.of(2025, 12, 1))
                        .endDate(LocalDate.of(2025, 12, 31))
                        .build();

                // stubbing
                given(dietPlanService.getDietPlanById(dietPlanId))
                        .willReturn(response);

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/{dietPlanId}", dietPlanId)
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.dietPlanId").value(dietPlanId))
                        .andExpect(jsonPath("$.title").value("다이어트 식단"))
                        .andExpect(jsonPath("$.content").value("건강한 다이어트를 위한 식단"))
                        .andExpect(jsonPath("$.primary").value(true))
                        .andExpect(jsonPath("$.startDate").value("2025-12-01"))
                        .andExpect(jsonPath("$.endDate").value("2025-12-31"));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 식단 계획 조회 시 404 응답이 반환된다.")
            @Test
            void notFoundDietPlan() throws Exception {
                // given
                Long dietPlanId = 999L;

                // stubbing
                given(dietPlanService.getDietPlanById(dietPlanId))
                        .willThrow(new DietPlanException(NOT_FOUND_DIET_PLAN));

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/{dietPlanId}", dietPlanId)
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
    @DisplayName("changePrimaryDietPlan")
    class ChangePrimaryDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 dietPlanId로 대표 식단 변경 시 204 No Content를 반환한다")
            void changePrimaryDietPlan() throws Exception {
                // given
                Long dietPlanId = 2L;

                // stubbing
                doNothing().when(dietPlanService).changePrimaryDietPlanTo(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/{dietPlanId}", dietPlanId)
                        )
                        .andDo(print())
                        .andExpect(status().isNoContent())
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("대표 식단으로 설정할 수 없는 경우 400 응답이 반환된다.")
            @Test
            void cannotSetAsPrimary() throws Exception {
                // given
                Long dietPlanId = 999L;

                // stubbing
                doThrow(new DietPlanException(CANNOT_SET_AS_PRIMARY))
                        .when(dietPlanService).changePrimaryDietPlanTo(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                patch("/api/diet-plans/{dietPlanId}", dietPlanId)
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획을 대표 식단 계획으로 설정할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("deleteDietPlan")
    class DeleteDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 dietPlanId로 삭제 시 204 No Content를 반환한다")
            void deleteDietPlan() throws Exception {
                // given
                Long dietPlanId = 1L;

                // stubbing
                doNothing().when(dietPlanService).deleteById(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/{dietPlanId}", dietPlanId)
                        )
                        .andDo(print())
                        .andExpect(status().isNoContent())
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 식단 계획 삭제 시 404 응답이 반환된다.")
            @Test
            void notFoundDietPlan() throws Exception {
                // given
                Long dietPlanId = 999L;

                // stubbing
                doThrow(new DietPlanException(NOT_FOUND_DIET_PLAN))
                        .when(dietPlanService).deleteById(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/{dietPlanId}", dietPlanId)
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("권한이 없는 사용자가 삭제 시도 시 403 응답이 반환된다.")
            @Test
            void unauthorized() throws Exception {
                // given
                Long dietPlanId = 1L;

                // stubbing
                doThrow(new DietPlanException(UNAUTHORIZED))
                        .when(dietPlanService).deleteById(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/diet-plans/{dietPlanId}", dietPlanId)
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("식단 계획 삭제 권한이 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getMyDietPlans")
    class GetMyDietPlans {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("내 식단 계획 목록 조회 시 200 OK와 식단 계획 리스트를 반환한다")
            void getMyDietPlans() throws Exception {
                // given
                DietPlanServiceResponse response1 = DietPlanServiceResponse.builder()
                        .dietPlanId(1L)
                        .title("다이어트 식단 1")
                        .content("내용 1")
                        .isPrimary(true)
                        .startDate(LocalDate.of(2025, 12, 1))
                        .endDate(LocalDate.of(2025, 12, 31))
                        .build();

                DietPlanServiceResponse response2 = DietPlanServiceResponse.builder()
                        .dietPlanId(2L)
                        .title("다이어트 식단 2")
                        .content("내용 2")
                        .isPrimary(false)
                        .startDate(LocalDate.of(2026, 1, 1))
                        .endDate(LocalDate.of(2026, 1, 31))
                        .build();

                // stubbing
                given(dietPlanService.getMyDietPlans(anyLong()))
                        .willReturn(List.of(response1, response2));

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/my")
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andExpect(jsonPath("$[0].dietPlanId").value(1L))
                        .andExpect(jsonPath("$[0].title").value("다이어트 식단 1"))
                        .andExpect(jsonPath("$[0].primary").value(true))
                        .andExpect(jsonPath("$[1].dietPlanId").value(2L))
                        .andExpect(jsonPath("$[1].primary").value(false));
            }

            @Test
            @DisplayName("식단 계획이 없을 경우 빈 배열을 반환한다")
            void getMyDietPlansEmpty() throws Exception {
                // given
                // stubbing
                given(dietPlanService.getMyDietPlans(anyLong()))
                        .willReturn(Collections.emptyList());

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/my")
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$").isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getPrimaryDietPlan")
    class GetPrimaryDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("대표 식단 계획 조회 시 200 OK와 대표 식단 정보를 반환한다")
            void getPrimaryDietPlan() throws Exception {
                // given
                DietPlanServiceResponse response = DietPlanServiceResponse.builder()
                        .dietPlanId(1L)
                        .title("대표 식단")
                        .content("현재 진행중인 식단")
                        .isPrimary(true)
                        .startDate(LocalDate.of(2025, 12, 1))
                        .endDate(LocalDate.of(2025, 12, 31))
                        .build();

                // stubbing
                given(dietPlanService.getPrimaryDietPlan(anyLong()))
                        .willReturn(response);

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/my/primary")
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.dietPlanId").value(1L))
                        .andExpect(jsonPath("$.title").value("대표 식단"))
                        .andExpect(jsonPath("$.primary").value(true));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("대표 식단이 없을 경우 404 응답이 반환된다.")
            @Test
            void notFoundPrimaryDietPlan() throws Exception {
                // given
                // stubbing
                given(dietPlanService.getPrimaryDietPlan(anyLong()))
                        .willThrow(new DietPlanException(NOT_FOUND_PRIMARY_DIET_PLAN));

                // when then
                mockMvc.perform(
                                get("/api/diet-plans/my/primary")
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("사용자의 대표 식단을 찾을 수 없습니다"))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }
}
