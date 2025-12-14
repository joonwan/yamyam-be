package com.ssafy.yamyam_coach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yamyam_coach.security.CustomUserDetailsService;
import com.ssafy.yamyam_coach.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

public abstract class RestControllerTestSupport {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;
}
