package com.ssafy.yamyam_coach.mapper.user;

import com.ssafy.yamyam_coach.domain.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    // 회원 저장 (void 대신 int를 쓰면 영향받은 행의 개수가 리턴돼서 성공 여부 체크 가능)
    int save(User user);

    // 이메일로 조회
    Optional<User> findByEmail(String email);

    // 닉네임 중복 체크
    boolean existsByNickname(String nickname);

    User findById(Long userId);

    List<User> searchByKeyword(@Param("keyword") String keyword);
}
