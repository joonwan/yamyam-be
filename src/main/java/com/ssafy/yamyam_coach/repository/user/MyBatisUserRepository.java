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
    public User save(User user) {
        userMapper.save(user);
        return user; // ID가 채워진 User 객체 리턴
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // 매퍼 결과를 Optional로 안전하게 감싸서 리턴 (팀 스타일)
        return userMapper.findByEmail(email); 
    }
    
    @Override
    public boolean existsByNickname(String nickname) {
        return userMapper.existsByNickname(nickname);
    }
}
