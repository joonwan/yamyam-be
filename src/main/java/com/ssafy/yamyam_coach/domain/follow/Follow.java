package com.ssafy.yamyam_coach.domain.follow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    private Long id;            // PK, Auto Increment
    
    private Long followerId;    // 팔로우 하는 사람 (나)
    private Long followedId;    // 팔로우 받는 사람 (상대방)
    
    private LocalDateTime createdAt; // 팔로우한 시간

    // ID 제외, 팔로우 관계 생성용 빌더
    @Builder
    private Follow(Long followerId, Long followedId, LocalDateTime createdAt) {
        this.followerId = followerId;
        this.followedId = followedId;
        this.createdAt = createdAt;
    }
}