package com.ssafy.yamyam_coach.mapper.user;

import com.ssafy.yamyam_coach.domain.user.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper {

    int insert(User user);

    User findById(Long userId);
}
