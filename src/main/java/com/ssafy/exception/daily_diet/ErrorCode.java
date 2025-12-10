package com.ssafy.exception.daily_diet;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_DATE("날짜가 식단 계획 기간을 벗어났습니다."),
    DUPLICATED_DATE("이미 해당 날짜의 식단이 존재합니다."),
    UNAUTHORIZED("일일 식단 제어 권한이 없습니다."),
    NOT_FOUND_DAILY_DIET("해당 일일 식단을 조회할 수 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
