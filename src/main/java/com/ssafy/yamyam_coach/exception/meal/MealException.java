package com.ssafy.yamyam_coach.exception.food;

import com.ssafy.yamyam_coach.exception.common.exception.CustomException;

public class FoodException extends CustomException {
    public FoodException(ErrorCode errorCode) {
        super(errorCode);
    }
}
