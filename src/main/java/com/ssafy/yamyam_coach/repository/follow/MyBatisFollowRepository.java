package com.ssafy.yamyam_coach.repository.follow;

import com.ssafy.yamyam_coach.domain.follow.Follow;
import com.ssafy.yamyam_coach.mapper.follow.FollowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MyBatisFollowRepository implements FollowRepository {

    private final FollowMapper followMapper;

    @Override
    public void save(Follow follow) {
        followMapper.save(follow);
    }

    @Override
    public void delete(Long followerId, Long followedId) {
        followMapper.delete(followerId, followedId);
    }

    @Override
    public boolean exists(Long followerId, Long followedId) {
        return followMapper.exists(followerId, followedId);
    }

    @Override
    public int countFollowers(Long userId) {
        return followMapper.countFollowers(userId);
    }

    @Override
    public int countFollowing(Long userId) {
        return followMapper.countFollowing(userId);
    }
}