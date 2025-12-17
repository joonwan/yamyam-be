package com.ssafy.yamyam_coach.mapper.follow;

import com.ssafy.yamyam_coach.domain.follow.Follow;
import com.ssafy.yamyam_coach.domain.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {
    void save(Follow follow); // 팔로우 하기
    void delete(@Param("followerId") Long followerId, @Param("followedId") Long followedId); // 언팔로우
    
    boolean exists(@Param("followerId") Long followerId, @Param("followedId") Long followedId); // 이미 팔로우했는지 확인

    int countFollowers(Long userId); // 나를 팔로우하는 사람 수 (Follower)
    int countFollowing(Long userId); // 내가 팔로우하는 사람 수 (Following)

    List<User> findFollowers(Long userId);
    List<User> findFollowing(Long userId);
}