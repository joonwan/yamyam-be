package com.ssafy.yamyam_coach.repository;

import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class DomainCreateHelper {

    public static User createUser(String name, String nickname, String email, String password) {
        return User.builder()
                .name(name)
                .nickname(nickname)
                .email(email)
                .password(password)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static DietPlan createDietPlan(Long userId, String title, String content, boolean isShared, boolean isPrimary, LocalDate startDate, LocalDate endDate) {
        return DietPlan.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .isShared(isShared)
                .isPrimary(isPrimary)
                .startDate(startDate)
                .endDate(endDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
