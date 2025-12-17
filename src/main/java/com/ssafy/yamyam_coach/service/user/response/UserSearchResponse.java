package com.ssafy.yamyam_coach.service.user.response; // 패키지 위치 확인!

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.yamyam_coach.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserSearchResponse {
    private Long id;
    private String email;
    private String nickname;
    private String name;
    @JsonProperty("isFollowing")
    private boolean isFollowing;

    @Builder
    public UserSearchResponse(User user, boolean isFollowing) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.name = user.getName();
        this.isFollowing = isFollowing;
    }
}