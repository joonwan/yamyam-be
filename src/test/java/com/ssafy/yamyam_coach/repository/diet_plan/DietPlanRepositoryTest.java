package com.ssafy.yamyam_coach.repository.diet_plan;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.ssafy.yamyam_coach.repository.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class DietPlanRepositoryTest extends IntegrationTestSupport {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DietPlanRepository dietPlanRepository;

    @DisplayName("식단 계획을 저장 할 수 있다.")
    @Test
    void insert() {
        // given
        User user = createUser("test user", "test nickname", "test@email.com", "password");
        userRepository.insert(user);

        DietPlan dietPlan = createDietPlan(user.getId(), "titile", "content", false, false, LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        // when
        Optional<DietPlan> findDietPlanOpt = dietPlanRepository.findById(dietPlan.getId());

        // then
        assertThat(findDietPlanOpt).isPresent();

        DietPlan findDietPlan = findDietPlanOpt.get();
        assertThat(findDietPlan.getTitle()).isEqualTo(dietPlan.getTitle());
        assertThat(findDietPlan.getContent()).isEqualTo(dietPlan.getContent());
        assertThat(findDietPlan.getStartDate()).isEqualTo(dietPlan.getStartDate());
        assertThat(findDietPlan.getEndDate()).isEqualTo(dietPlan.getEndDate());
    }

    @DisplayName("사용자 pk 기반으로 식단 계획들을 조회할 수 있다.")
    @Test
    void findDietPlansByUserId() {

        // given
        User user = createUser("test user", "test nickname", "test@email.com", "password");
        userRepository.insert(user);

        DietPlan dietPlan1 = createDietPlan(user.getId(), "titile", "content", false, true, LocalDate.now(), LocalDate.now().plusDays(1));
        DietPlan dietPlan2 = createDietPlan(user.getId(), "titile2", "content2", false, false, LocalDate.now(), LocalDate.now().plusDays(1));

        dietPlanRepository.insert(dietPlan1);
        dietPlanRepository.insert(dietPlan2);

        // when
        List<DietPlan> plans = dietPlanRepository.findDietPlansByUserId(user.getId());

        // then
        assertThat(plans).hasSize(2)
                .extracting(DietPlan::getId)
                .containsExactlyInAnyOrder(dietPlan1.getId(), dietPlan2.getId());
    }

    @DisplayName("pk 기반으로 해당 diet plan 이 존재하는지 확인할 수 있다.")
    @Test
    void existsById() {
        // given
        User user = createUser("test user", "test nickname", "test@email.com", "password");
        userRepository.insert(user);

        DietPlan dietPlan = createDietPlan(user.getId(), "titile", "content", false, true, LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        // when
        boolean existsResult = dietPlanRepository.existsById(dietPlan.getId());
        boolean notExistsResult = dietPlanRepository.existsById(1000L);

        // then
        assertThat(existsResult).isTrue();
        assertThat(notExistsResult).isFalse();
    }

    @DisplayName("pk 기반으로 단건 삭제할 수 있다.")
    @Test
    void deleteById() {
        // given
        User user = createUser("test user", "test nickname", "test@email.com", "password");
        userRepository.insert(user);

        DietPlan dietPlan = createDietPlan(user.getId(), "titile", "content", false, true, LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        // when
        dietPlanRepository.deleteById(dietPlan.getId());
        Optional<DietPlan> findDietPlanOpt = dietPlanRepository.findById(dietPlan.getId());

        // then
        assertThat(findDietPlanOpt).isEmpty();
    }

    @DisplayName("사용자의 대표 식단을 조회할 수 있다.")
    @Test
    void findUsersPrimaryDietPlan() {
        // given
        User user = createUser("test user", "test nickname", "test@email.com", "password");
        userRepository.insert(user);

        DietPlan dietPlan1 = createDietPlan(user.getId(), "titile", "content", false, false, LocalDate.now(), LocalDate.now().plusDays(1));
        DietPlan dietPlan2 = createDietPlan(user.getId(), "titile2", "content2", false, true, LocalDate.now(), LocalDate.now().plusDays(1));

        dietPlanRepository.insert(dietPlan1);
        dietPlanRepository.insert(dietPlan2);

        // when
        Optional<DietPlan> primaryDietPlanOpt = dietPlanRepository.findUsersPrimaryDietPlan(user.getId());

        //then
        assertThat(primaryDietPlanOpt).isPresent();

        DietPlan primaryDietPlan = primaryDietPlanOpt.get();
        assertThat(primaryDietPlan.getId()).isEqualTo(dietPlan2.getId());
        assertThat(primaryDietPlan.getTitle()).isEqualTo(dietPlan2.getTitle());
        assertThat(primaryDietPlan.getContent()).isEqualTo(dietPlan2.getContent());
        assertThat(primaryDietPlan.getStartDate()).isEqualTo(dietPlan2.getStartDate());
        assertThat(primaryDietPlan.getEndDate()).isEqualTo(dietPlan2.getEndDate());
    }

    @DisplayName("사용자의 대표 식단을 비활성화 할 수 있다.")
    @Test
    void deActivateCurrentPrimaryDietPlan() {
        // given
        User user = createUser("test user", "test nickname", "test@email.com", "password");
        userRepository.insert(user);

        DietPlan dietPlan = createDietPlan(user.getId(), "titile", "content", false, true, LocalDate.now(), LocalDate.now().plusDays(1));

        dietPlanRepository.insert(dietPlan);

        // when
        dietPlanRepository.deActivateCurrentPrimaryDietPlan(user.getId());
        Optional<DietPlan> usersPrimaryDietPlanOpt = dietPlanRepository.findUsersPrimaryDietPlan(user.getId());

        //then
        assertThat(usersPrimaryDietPlanOpt).isEmpty();
    }

    @DisplayName("특정 식단을 사용자의 대표식단으로 지정할 수 있다.")
    @Test
    void activateCurrentPrimaryDietPlan() {
        // given
        User user = createUser("test user", "test nickname", "test@email.com", "password");
        userRepository.insert(user);

        DietPlan dietPlan = createDietPlan(user.getId(), "titile", "content", false, false, LocalDate.now(), LocalDate.now().plusDays(1));

        dietPlanRepository.insert(dietPlan);

        // when
        dietPlanRepository.activateCurrentPrimaryDietPlan(user.getId(), dietPlan.getId());
        Optional<DietPlan> usersPrimaryDietPlanOpt = dietPlanRepository.findUsersPrimaryDietPlan(user.getId());

        //then
        assertThat(usersPrimaryDietPlanOpt).isPresent();
        DietPlan primaryDietPlan = usersPrimaryDietPlanOpt.get();
        assertThat(primaryDietPlan.getId()).isEqualTo(dietPlan.getId());
        assertThat(primaryDietPlan.getTitle()).isEqualTo(dietPlan.getTitle());
        assertThat(primaryDietPlan.getContent()).isEqualTo(dietPlan.getContent());
        assertThat(primaryDietPlan.getStartDate()).isEqualTo(dietPlan.getStartDate());
        assertThat(primaryDietPlan.getEndDate()).isEqualTo(dietPlan.getEndDate());
    }
}
