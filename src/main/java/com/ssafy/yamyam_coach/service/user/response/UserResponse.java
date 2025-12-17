package com.ssafy.yamyam_coach.service.user.response;

import com.ssafy.yamyam_coach.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {

    private String email;
    private String nickname;
    private String name;
    
    // ★ 팔로우 기능 추가하면서 새로 생긴 필드들!
    private int followers; // 나를 팔로우하는 사람 수
    private int following; // 내가 팔로우하는 사람 수

    @Builder
    private UserResponse(String email, String nickname, String name, int followers, int following) {
        this.email = email;
        this.nickname = nickname;
        this.name = name;
        this.followers = followers;
        this.following = following;
    }

    // User 엔티티 -> UserResponse DTO 변환 (팔로우 숫자 포함)
    public static UserResponse of(User user, int followers, int following) {
        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .followers(followers)
                .following(following)
                .build();
    }
    
    // (선택) 팔로우 숫자를 아직 모를 때 쓰는 간편 변환 메서드 (기본값 0)
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .followers(0)
                .following(0)
                .build();
    }
}