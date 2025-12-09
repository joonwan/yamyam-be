package com.ssafy.yamyam_coach.repository.user;

import com.ssafy.yamyam_coach.domain.user.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user); // 저장된 객체를 리턴하거나, ID를 리턴하거나 설계 나름

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);
}
