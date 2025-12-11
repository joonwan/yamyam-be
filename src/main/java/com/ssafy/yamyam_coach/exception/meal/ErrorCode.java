package com.ssafy.yamyam_coach.exception.food;

import com.ssafy.yamyam_coach.exception.common.errorcode.CustomErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements CustomErrorCode {
    NOT_FOUND_FOOD("해당 음식을 조회할 수 없습니다.",  HttpStatus.NOT_FOUND),;

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
