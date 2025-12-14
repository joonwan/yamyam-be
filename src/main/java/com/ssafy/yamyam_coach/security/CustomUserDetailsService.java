package com.ssafy.yamyam_coach.security;

import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. DB에서 이메일로 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다: " + email));

        // 2. Spring Security가 이해할 수 있는 UserDetails 객체로 변환해서 리턴
        // (여기서는 권한을 일단 "ROLE_USER"로 통일했어)
        return new CustomUserDetails(user);
    }
}