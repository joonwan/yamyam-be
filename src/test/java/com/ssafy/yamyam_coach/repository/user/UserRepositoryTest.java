package com.ssafy.yamyam_coach.repository.user;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.ssafy.yamyam_coach.repository.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends IntegrationTestSupport {

    @Autowired
    UserRepository userRepository;

    @DisplayName("사용자를 저장할 수 있다.")
    @Test
    void insert() {

        //given
        User user = createUser("test user", "test nickname", "test@email.com", "password");
        userRepository.insert(user);

        //when
        Optional<User> findUserOpt = userRepository.findById(user.getId());

        //then
        assertThat(findUserOpt).isPresent();

        User findUser = findUserOpt.get();
        assertThat(findUser.getName()).isEqualTo(user.getName());
        assertThat(findUser.getNickname()).isEqualTo(user.getNickname());
        assertThat(findUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(findUser.getPassword()).isEqualTo(user.getPassword());
    }

}