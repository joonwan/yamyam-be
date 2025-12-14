package com.ssafy.yamyam_coach.security;

import com.ssafy.yamyam_coach.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    // ★ 핵심: 우리의 진짜 User 객체를 품고 있음
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 필수 오버라이드 메서드들 (이메일, 비밀번호 연결)
    @Override
    public String getUsername() { return user.getEmail(); }
    
    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 권한 없으면 빈 리스트
    }

    // 계정 상태 관리 (복잡한 로직 없으면 무조건 true)
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}