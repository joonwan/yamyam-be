package com.ssafy.yamyam_coach.repository.user;

import com.ssafy.yamyam_coach.domain.user.User;

import java.util.Optional;

public interface UserRepository {

    int insert(User user);

    Optional<User> findById(Long userId);
}
