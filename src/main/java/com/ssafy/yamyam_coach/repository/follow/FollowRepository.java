package com.ssafy.yamyam_coach.repository.follow;

import com.ssafy.yamyam_coach.domain.follow.Follow;

public interface FollowRepository {
    void save(Follow follow);
    void delete(Long followerId, Long followedId);
    boolean exists(Long followerId, Long followedId);
    int countFollowers(Long userId);
    int countFollowing(Long userId);
}