package com.ssafy.yamyam_coach.domain.body_spec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BodySpec {

    private Long id;          // PK, Auto Increment
    private Long userId;      // FK (users 테이블의 id)

    private int height;       // 키 (cm)
    private int weight;       // 체중 (kg)
    private int age;          // 나이
    private String gender;    // 성별 ("MALE", "FEMALE" 문자열 저장)

    private LocalDateTime created_at; // 측정일 (DATETIME)

    // ID는 DB 자동 생성이므로 제외, 측정 데이터 입력용 빌더
    @Builder
    private BodySpec(Long userId, int height, int weight, int age, String gender, LocalDateTime created_at) {
        this.userId = userId;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.created_at = created_at;
    }
}