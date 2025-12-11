package com.ssafy.yamyam_coach.exception.diet_plan;

import com.ssafy.yamyam_coach.exception.common.errorcode.CustomErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements CustomErrorCode {
    NOT_FOUND_DIET_PLAN("해당 식단 계획을 조회할 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_PRIMARY_DIET_PLAN("사용자의 대표 식단을 찾을 수 없습니다",  HttpStatus.NOT_FOUND),
    UNAUTHORIZED("식단 계획 삭제 권한이 없습니다.",  HttpStatus.FORBIDDEN),
    CANNOT_SET_AS_PRIMARY("해당 식단 계획을 대표 식단 계획으로 설정할 수 없습니다.",   HttpStatus.BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
