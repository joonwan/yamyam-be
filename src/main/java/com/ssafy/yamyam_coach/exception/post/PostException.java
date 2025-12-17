package com.ssafy.yamyam_coach.exception.post;

import com.ssafy.yamyam_coach.exception.common.exception.CustomException;

public class PostException extends CustomException {
    public PostException(PostErrorCode errorCode) {
        super(errorCode);
    }
}
