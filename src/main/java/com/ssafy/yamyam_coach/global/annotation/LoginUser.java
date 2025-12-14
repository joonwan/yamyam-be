package com.ssafy.yamyam_coach.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 파라미터에만 붙인다
@Retention(RetentionPolicy.RUNTIME) // 실행 중에도 계속 유지된다
public @interface LoginUser {
}