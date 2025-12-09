package com.ssafy.yamyam_coach.repository.user;

import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisUserRepository implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public int insert(User user) {
        return userMapper.insert(user);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(userMapper.findById(userId));
    }
}
