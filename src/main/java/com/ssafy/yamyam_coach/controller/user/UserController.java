package com.ssafy.yamyam_coach.controller.user;

import com.ssafy.yamyam_coach.controller.user.request.JoinRequest;
import com.ssafy.yamyam_coach.controller.user.request.LoginRequest;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.global.annotation.LoginUser;
import com.ssafy.yamyam_coach.service.user.UserService;
import com.ssafy.yamyam_coach.service.user.response.UserLoginServiceResponse;
import com.ssafy.yamyam_coach.service.user.response.UserResponse;
import com.ssafy.yamyam_coach.service.user.response.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody JoinRequest request) {
        userService.signup(request.toServiceDto());
        return ResponseEntity.ok("회원가입 성공!");
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginServiceResponse> login(@RequestBody LoginRequest request) {
        // 로그인은 입력값이 적어서 Service DTO 없이 바로 파라미터로 넘겨도 됨 (팀 스타일에 따라 유연하게)
        UserLoginServiceResponse response = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@LoginUser User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }

        // 서비스에게 "이 유저 프로필 정보 줘" 하고 시키기
        return ResponseEntity.ok(userService.getUserProfile(user));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @LoginUser User user,
            @RequestParam String keyword) {

        // 로그인 안 했으면 401 에러
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        // 서비스로 검색 요청 (내 ID도 같이 넘겨서 팔로우 여부 확인)
        List<UserSearchResponse> result = userService.searchUsers(keyword, user.getId());

        return ResponseEntity.ok(result);
    }
}