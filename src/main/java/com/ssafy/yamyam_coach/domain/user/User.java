package com.ssafy.yamyam_coach.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public User(String email, String password, String nickname, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}