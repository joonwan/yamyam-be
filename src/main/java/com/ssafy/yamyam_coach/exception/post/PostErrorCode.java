package com.ssafy.yamyam_coach.exception.post;

import com.ssafy.yamyam_coach.exception.common.errorcode.CustomErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PostErrorCode implements CustomErrorCode {
    NOT_FOUND_POST("해당 게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    ;

    private final String message;
    private final HttpStatus httpStatus;

    PostErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
