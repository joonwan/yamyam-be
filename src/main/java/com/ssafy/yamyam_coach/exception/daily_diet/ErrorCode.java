package com.ssafy.yamyam_coach.exception.daily_diet;

import com.ssafy.yamyam_coach.exception.common.errorcode.CustomErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements CustomErrorCode {
    INVALID_DATE("날짜가 식단 계획 기간을 벗어났습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED_DATE("이미 해당 날짜의 식단이 존재합니다.", HttpStatus.CONFLICT),
    UNAUTHORIZED("일일 식단 제어 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND_DAILY_DIET("해당 일일 식단을 조회할 수 없습니다.", HttpStatus.NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
