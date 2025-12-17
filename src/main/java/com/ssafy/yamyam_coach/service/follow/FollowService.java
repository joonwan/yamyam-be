package com.ssafy.yamyam_coach.service.follow;

import com.ssafy.yamyam_coach.domain.follow.Follow;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.mapper.follow.FollowMapper;
import com.ssafy.yamyam_coach.mapper.user.UserMapper;
import com.ssafy.yamyam_coach.repository.follow.FollowRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository; // 유저 존재 확인용
import com.ssafy.yamyam_coach.service.user.response.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;

    // 팔로우 하기
    @Transactional
    public void follow(Long followerId, Long followedId) {
        // 1. 자기 자신 팔로우 금지
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }

        // 2. 상대방이 존재하는지 확인
        userRepository.findById(followedId) // findById가 필요해짐!
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 3. 이미 팔로우 중인지 확인 (중복 방지)
        if (followRepository.exists(followerId, followedId)) {
            throw new IllegalArgumentException("이미 팔로우 중입니다.");
        }

        // 4. 저장
        Follow follow = Follow.builder()
                .followerId(followerId)
                .followedId(followedId)
                .createdAt(LocalDateTime.now())
                .build();
        
        followRepository.save(follow);
    }

    // 언팔로우 하기
    @Transactional
    public void unfollow(Long followerId, Long followedId) {
        followRepository.delete(followerId, followedId);
    }

    @Transactional(readOnly = true)
    public List<UserSearchResponse> getFollowers(Long myId) {
        // 1. 나를 팔로우하는 유저들 조회 (Mapper 호출)
        List<User> followers = followMapper.findFollowers(myId);

        // 2. DTO 변환 (내가 맞팔로우 중인지 확인)
        return followers.stream()
                .map(user -> {
                    // 내가(myId) -> 이 사람(user.getId())을 팔로우 중인가?
                    boolean isFollowing = followRepository.exists(myId, user.getId());
                    return new UserSearchResponse(user, isFollowing);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserSearchResponse> getFollowing(Long myId) {
        // 1. 내가 팔로우하는 유저들 조회
        List<User> followingUsers = followMapper.findFollowing(myId);

        // 2. DTO 변환 (팔로잉 목록이니까 isFollowing은 무조건 true)
        return followingUsers.stream()
                .map(user -> new UserSearchResponse(user, true))
                .collect(Collectors.toList());
    }
}