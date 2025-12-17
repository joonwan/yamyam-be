package com.ssafy.yamyam_coach.service.diet_plan;

import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import com.ssafy.yamyam_coach.service.diet_plan.request.CreateDietPlanServiceRequest;
import com.ssafy.yamyam_coach.service.diet_plan.request.UpdateDietPlanServiceRequest;
import com.ssafy.yamyam_coach.service.diet_plan.response.DietPlanServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static com.ssafy.yamyam_coach.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DietPlanServiceTest extends IntegrationTestSupport {

    @Autowired
    DietPlanService dietPlanService;

    @Autowired
    DietPlanRepository dietPlanRepository;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("registerDietPlan")
    class RegisterDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("첫번째 등록되는 식단 계획은 대표 식단이 된다.")
            @Test
            void firstDietPlanBecomePrimary() {

                //given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest request = CreateDietPlanServiceRequest.builder()
                        .title("title")
                        .content("content")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                // when
                Long dietPlanId = dietPlanService.registerDietPlan(user.getId(), request);
                DietPlan findDietPlan = dietPlanRepository.findById(dietPlanId).orElse(null);

                //then
                assertThat(findDietPlan).isNotNull();
                assertThat(findDietPlan.getTitle()).isEqualTo("title");
                assertThat(findDietPlan.getContent()).isEqualTo("content");
                assertThat(findDietPlan.isPrimary()).isTrue();
            }

            @DisplayName("이미 대표 식단이 있을 경우 새로운 식단을 등록하면 새로운 식단은 대표 식단이 아니다.")
            @Test
            void secondDietPlanIsNotPrimary() {

                //given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest request1 = CreateDietPlanServiceRequest.builder()
                        .title("title1")
                        .content("content1")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                CreateDietPlanServiceRequest request2 = CreateDietPlanServiceRequest.builder()
                        .title("title2")
                        .content("content2")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                // when
                Long dietPlanId1 = dietPlanService.registerDietPlan(user.getId(), request1);
                Long dietPlanId2 = dietPlanService.registerDietPlan(user.getId(), request2);
                DietPlan findDietPlan1 = dietPlanRepository.findById(dietPlanId1).orElse(null);
                DietPlan findDietPlan2 = dietPlanRepository.findById(dietPlanId2).orElse(null);

                //then
                assertThat(findDietPlan1).isNotNull();
                assertThat(findDietPlan2).isNotNull();
                assertThat(findDietPlan1.getTitle()).isEqualTo("title1");
                assertThat(findDietPlan2.getTitle()).isEqualTo("title2");
                assertThat(findDietPlan1.getContent()).isEqualTo("content1");
                assertThat(findDietPlan2.getContent()).isEqualTo("content2");
                assertThat(findDietPlan1.isPrimary()).isTrue();
                assertThat(findDietPlan2.isPrimary()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("Diet plan 이 존재하고 요청자의 Diet plan 일 경우 삭제가 성공한다.")
            @Test
            void successDeleteDietPlan() {

                //given

                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                //when
                dietPlanService.deleteById(user.getId(), dietPlan.getId());
                DietPlan findDietPlan = dietPlanRepository.findById(dietPlan.getId()).orElse(null);

                //then
                assertThat(findDietPlan).isNull();

            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("Diet Plan 이 존재하지 않으면 NOT_FOUND_DIET_PLAN 예외가 발생한다.")
            @Test
            void notFoundDietPlan() {
                //given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistsDietPlanId = 999999L;

                //when //then
                assertThatThrownBy(() -> dietPlanService.deleteById(user.getId(), notExistsDietPlanId))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 조회할 수 없습니다.");

            }

            @DisplayName("요청자 자신의 Diet plan 이 아닌 Diet plan 을 삭제 시도하면 UNAUTHORIZED_FOR_DELETE 예외가 발생한다.")
            @Test
            void unAuthorizedDietPlan() {
                //given
                User user = createDummyUser();
                userRepository.save(user);

                User other = createUser("other", "other nickname", "다른사람@test.com", "password");
                userRepository.save(other);

                DietPlan othersDietPlan = createDummyDietPlan(other.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(othersDietPlan);

                //when //then
                assertThatThrownBy(() -> dietPlanService.deleteById(user.getId(), othersDietPlan.getId()))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("식단 계획 삭제 권한이 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("changePrimaryDietPlanTo")
    class ChangePrimaryDietPlanTo {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("사용자의 대표 식단 계획을 변경할 수 있다.")
            @Test
            void changePrimaryDietPlanTo() {
                //given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest request1 = CreateDietPlanServiceRequest.builder()
                        .title("title1")
                        .content("content1")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                CreateDietPlanServiceRequest request2 = CreateDietPlanServiceRequest.builder()
                        .title("title2")
                        .content("content2")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                Long dietPlanId1 = dietPlanService.registerDietPlan(user.getId(), request1);
                Long dietPlanId2 = dietPlanService.registerDietPlan(user.getId(), request2);

                //when
                dietPlanService.changePrimaryDietPlanTo(user.getId(), dietPlanId2);
                Long changedPrimaryDietPlanId = dietPlanService.getPrimaryDietPlan(user.getId()).getDietPlanId();

                //then
                assertThat(changedPrimaryDietPlanId).isEqualTo(dietPlanId2);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("없는 DietPlan 을 대표식단으로 변경 시도시 CANNOT_SET_AS_PRIMARY 예외가 발생한다.")
            @Test
            void updateDietPlanToNotExistDietPlan() {
                //given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest request = CreateDietPlanServiceRequest.builder()
                        .title("title1")
                        .content("content1")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                dietPlanService.registerDietPlan(user.getId(), request);

                //when //then
                assertThatThrownBy(() -> dietPlanService.changePrimaryDietPlanTo(user.getId(), 9999L))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 대표 식단 계획으로 설정할 수 없습니다.");
            }

            @DisplayName("다른 사람의 DietPlan 으로 식단 변경 시도시 CANNOT_SET_AS_PRIMARY 예외가 발생한다.")
            @Test
            void updateDietPlanToOthersDietPlan() {
                //given
                User user = createDummyUser();
                userRepository.save(user);

                User other = createUser("other", "other nickname", "다른사람@test.com", "password");
                userRepository.save(other);

                CreateDietPlanServiceRequest request = CreateDietPlanServiceRequest.builder()
                        .title("title1")
                        .content("content1")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                Long othersDietPlanId = dietPlanService.registerDietPlan(other.getId(), request);

                //when //then
                assertThatThrownBy(() -> dietPlanService.changePrimaryDietPlanTo(user.getId(), othersDietPlanId))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 대표 식단 계획으로 설정할 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("getMyDietPlans")
    class GetMyDietPlans {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("자신의 모든 식단 계획을 조회할 수 있다.")
            @Test
            void getMyDietPlans() {
                //given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest request1 = CreateDietPlanServiceRequest.builder()
                        .title("title1")
                        .content("content1")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                CreateDietPlanServiceRequest request2 = CreateDietPlanServiceRequest.builder()
                        .title("title2")
                        .content("content2")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                CreateDietPlanServiceRequest request3 = CreateDietPlanServiceRequest.builder()
                        .title("title3")
                        .content("content3")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                Long dietPlan1Id = dietPlanService.registerDietPlan(user.getId(), request1);
                Long dietPlan2Id = dietPlanService.registerDietPlan(user.getId(), request2);
                Long dietPlan3Id = dietPlanService.registerDietPlan(user.getId(), request3);

                //when
                List<DietPlanServiceResponse> findDietPlans = dietPlanService.getMyDietPlans(user.getId());

                //then
                assertThat(findDietPlans).hasSize(3)
                        .extracting(DietPlanServiceResponse::getDietPlanId)
                        .containsExactlyInAnyOrder(dietPlan1Id, dietPlan2Id, dietPlan3Id);
            }


            @DisplayName("자신의 식단이 없을 경우 빈 리스트가 반환된다.")
            @Test
            void getMyEmptyDietPlans() {
                //given
                User user = createDummyUser();
                userRepository.save(user);

                //when
                List<DietPlanServiceResponse> findDietPlans = dietPlanService.getMyDietPlans(user.getId());

                //then
                assertThat(findDietPlans).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getDietPlanById")
    class GetDietPlanById {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("Diet plan 의 id 기반으로 조회할 수 있다.")
            @Test
            void getDietPlanById() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest request = CreateDietPlanServiceRequest.builder()
                        .title("title1")
                        .content("content1")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                Long savedDietPlanId = dietPlanService.registerDietPlan(user.getId(), request);

                // when
                DietPlanServiceResponse response = dietPlanService.getDietPlanById(savedDietPlanId);

                // then
                assertThat(response.getDietPlanId()).isEqualTo(savedDietPlanId);
                assertThat(response.getTitle()).isEqualTo("title1");
                assertThat(response.getContent()).isEqualTo("content1");
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("없는 Diet plan id 로 조회시 NOT_FOUND_DIET_PLAN 예외가 발생한다.")
            @Test
            void getDietPlanByIdWithNotExistDietPlanId() {
                // given
                Long notExistDietPlanId = 9999L;

                // when // then
                assertThatThrownBy(() -> dietPlanService.getDietPlanById(notExistDietPlanId))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 조회할 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("GetPrimaryDietPlan")
    class GetPrimaryDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("사용자의 대표 식단을 조회할 수 있다.")
            @Test
            void getPrimaryDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest request = CreateDietPlanServiceRequest.builder()
                        .title("title1")
                        .content("content1")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();

                Long savedDietPlanId = dietPlanService.registerDietPlan(user.getId(), request);

                // when
                DietPlanServiceResponse primaryDietPlan = dietPlanService.getPrimaryDietPlan(user.getId());

                // then
                assertThat(primaryDietPlan.getDietPlanId()).isEqualTo(savedDietPlanId);
            }

            @DisplayName("사용자 대표 식단이 없을 경우 빈 대표식단이 반환된다.")
            @Test
            void notExistsPrimaryDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                //when
                DietPlanServiceResponse primaryDietPlan = dietPlanService.getPrimaryDietPlan(user.getId());

                //then
                assertThat(primaryDietPlan.getDietPlanId()).isNull();
                assertThat(primaryDietPlan.getTitle()).isEqualTo(null);
                assertThat(primaryDietPlan.getContent()).isEqualTo(null);
                assertThat(primaryDietPlan.getStartDate()).isNull();
                assertThat(primaryDietPlan.getEndDate()).isNull();
                assertThat(primaryDietPlan.isPrimary()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("updateDietPlan")
    class UpdateDietPlan {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("Content 필드만 업데이트 할 수 있다. (Partial Update)")
            @Test
            void updateOnlyContent() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate originalStart = LocalDate.of(2025, 1, 1);
                LocalDate originalEnd = LocalDate.of(2025, 1, 10);

                // 기존 식단 계획 생성
                CreateDietPlanServiceRequest originalRequest = CreateDietPlanServiceRequest.builder()
                        .title("title")
                        .content("Old Content")
                        .startDate(originalStart)
                        .endDate(originalEnd)
                        .build();
                Long dietPlanId = dietPlanService.registerDietPlan(user.getId(), originalRequest);

                // 업데이트 요청: Content만 변경하고 나머지 필드는 null (PATCH 특성)
                String newContent = "New Content Updated";
                UpdateDietPlanServiceRequest request = createUpdateRequest(dietPlanId, newContent, null, null);

                // when
                dietPlanService.updateDietPlan(user.getId(), request);
                DietPlan findDietPlan = dietPlanRepository.findById(dietPlanId).orElseThrow();

                // then
                // 1. Content만 변경되었는지 확인
                assertThat(findDietPlan.getContent()).isEqualTo(newContent);
                // 2. 날짜는 기존 값 그대로 유지되었는지 확인
                assertThat(findDietPlan.getStartDate()).isEqualTo(originalStart);
                assertThat(findDietPlan.getEndDate()).isEqualTo(originalEnd);
            }

            @DisplayName("날짜 범위만 업데이트 할 수 있다.")
            @Test
            void updateOnlyDatesAndCheckDailyDietDeletion() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                LocalDate originalStart = LocalDate.of(2025, 1, 1);
                LocalDate originalEnd = LocalDate.of(2025, 1, 10);

                CreateDietPlanServiceRequest originalRequest = CreateDietPlanServiceRequest.builder()
                        .title("title")
                        .content("Content")
                        .startDate(originalStart)
                        .endDate(originalEnd)
                        .build();
                Long dietPlanId = dietPlanService.registerDietPlan(user.getId(), originalRequest);

                LocalDate newStart = LocalDate.of(2025, 1, 5);
                LocalDate newEnd = LocalDate.of(2025, 1, 15);

                UpdateDietPlanServiceRequest request = createUpdateRequest(dietPlanId, null, newStart, newEnd);

                // when
                dietPlanService.updateDietPlan(user.getId(), request);
                DietPlan findDietPlan = dietPlanRepository.findById(dietPlanId).orElseThrow();

                // then
                assertThat(findDietPlan.getContent()).isEqualTo("Content");
                assertThat(findDietPlan.getStartDate()).isEqualTo(newStart);
                assertThat(findDietPlan.getEndDate()).isEqualTo(newEnd);
            }

            @DisplayName("날짜와 Content 필드를 동시에 업데이트 할 수 있다.")
            @Test
            void updateAllFields() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest originalRequest = CreateDietPlanServiceRequest.builder()
                        .title("title")
                        .content("Old Content")
                        .startDate(LocalDate.of(2025, 1, 1))
                        .endDate(LocalDate.of(2025, 1, 10))
                        .build();
                Long dietPlanId = dietPlanService.registerDietPlan(user.getId(), originalRequest);

                String newContent = "New Content";
                LocalDate newStart = LocalDate.of(2025, 2, 1);
                LocalDate newEnd = LocalDate.of(2025, 2, 28);
                UpdateDietPlanServiceRequest request = createUpdateRequest(dietPlanId, newContent, newStart, newEnd);

                // when
                dietPlanService.updateDietPlan(user.getId(), request);
                DietPlan findDietPlan = dietPlanRepository.findById(dietPlanId).orElseThrow();

                // then
                assertThat(findDietPlan.getContent()).isEqualTo(newContent);
                assertThat(findDietPlan.getStartDate()).isEqualTo(newStart);
                assertThat(findDietPlan.getEndDate()).isEqualTo(newEnd);
            }

            @DisplayName("변경 사항이 없으면 DB 업데이트 없이 바로 return 한다.")
            @Test
            void returnImmediatelyWhenNoChange() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                String content = "Original Content";
                LocalDate start = LocalDate.of(2025, 3, 1);
                LocalDate end = LocalDate.of(2025, 3, 10);

                CreateDietPlanServiceRequest originalRequest = CreateDietPlanServiceRequest.builder()
                        .title("title")
                        .content(content)
                        .startDate(start)
                        .endDate(end)
                        .build();
                Long dietPlanId = dietPlanService.registerDietPlan(user.getId(), originalRequest);

                UpdateDietPlanServiceRequest request = createUpdateRequest(dietPlanId, content, start, end);

                // when
                dietPlanService.updateDietPlan(user.getId(), request);

                // then
                DietPlan findDietPlan = dietPlanRepository.findById(dietPlanId).orElse(null);
                assertThat(findDietPlan).isNotNull();
                assertThat(findDietPlan.getContent()).isEqualTo(content);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 DietPlan ID로 업데이트 시도시 NOT_FOUND_DIET_PLAN 예외가 발생한다.")
            @Test
            void notFoundDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistsDietPlanId = 999999L;
                UpdateDietPlanServiceRequest request = createUpdateRequest(notExistsDietPlanId, "Content", null, null);

                // when //then
                assertThatThrownBy(() -> dietPlanService.updateDietPlan(user.getId(), request))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 조회할 수 없습니다.");
            }

            @DisplayName("다른 사용자의 DietPlan 업데이트 시도시 UNAUTHORIZED_FOR_DELETE 예외가 발생한다.")
            @Test
            void unauthorizedDietPlan() {
                // given
                User user = createDummyUser(); // 요청자
                userRepository.save(user);

                User other = createUser("other", "other nickname", "다른사람@test.com", "password");
                userRepository.save(other);

                CreateDietPlanServiceRequest originalRequest = CreateDietPlanServiceRequest.builder()
                        .title("title")
                        .content("Old Content")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(1))
                        .build();
                Long othersDietPlanId = dietPlanService.registerDietPlan(other.getId(), originalRequest);

                UpdateDietPlanServiceRequest request = createUpdateRequest(othersDietPlanId, "New Content", null, null);

                // when //then
                // 다른 사용자 ID로 업데이트 시도
                assertThatThrownBy(() -> dietPlanService.updateDietPlan(user.getId(), request))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("식단 계획 삭제 권한이 없습니다."); // 예외 메시지 확인
            }

            @DisplayName("날짜 변경 시 DailyDiet 정리 로직에서 NPE가 발생하지 않는지 확인한다.")
            @Test
            void dateDeletionNpePrevention() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                CreateDietPlanServiceRequest originalRequest = CreateDietPlanServiceRequest.builder()
                        .title("title")
                        .content("Content")
                        .startDate(LocalDate.of(2025, 1, 1))
                        .endDate(LocalDate.of(2025, 1, 10))
                        .build();
                Long dietPlanId = dietPlanService.registerDietPlan(user.getId(), originalRequest);

                // 업데이트 요청: StartDate만 변경하고 EndDate는 null로 보냄 (NPE 발생 위험 시나리오)
                LocalDate newStart = LocalDate.of(2025, 1, 5);

                // Content도 변경 요청 (로직 실행을 강제하기 위해)
                UpdateDietPlanServiceRequest request = createUpdateRequest(dietPlanId, "New Content", newStart, null);

                // when
                dietPlanService.updateDietPlan(user.getId(), request);

                DietPlan findDietPlan = dietPlanRepository.findById(dietPlanId).orElse(null);

                //then
                assertThat(findDietPlan).isNotNull();
                assertThat(findDietPlan.getContent()).isEqualTo("New Content");
                assertThat(findDietPlan.getStartDate()).isEqualTo(newStart);
                assertThat(findDietPlan.getEndDate()).isEqualTo(LocalDate.of(2025, 1, 10));
            }
        }
    }

    private UpdateDietPlanServiceRequest createUpdateRequest(Long dietPlanId, String content, LocalDate startDate, LocalDate endDate) {
        return UpdateDietPlanServiceRequest.builder()
                .dietPlanId(dietPlanId)
                .content(content)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}