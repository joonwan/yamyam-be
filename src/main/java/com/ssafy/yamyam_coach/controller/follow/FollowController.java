package com.ssafy.yamyam_coach.controller.follow;

import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.global.annotation.LoginUser;
import com.ssafy.yamyam_coach.service.follow.FollowService;
import com.ssafy.yamyam_coach.service.user.response.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우 하기 (POST /api/follows/{targetId})
    @PostMapping("/{targetId}")
    public ResponseEntity<String> follow(@LoginUser User user, @PathVariable Long targetId) {
        if (user == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        
        followService.follow(user.getId(), targetId);
        return ResponseEntity.ok("팔로우 했습니다.");
    }

    // 언팔로우 하기 (DELETE /api/follows/{targetId})
    @DeleteMapping("/{targetId}")
    public ResponseEntity<String> unfollow(@LoginUser User user, @PathVariable Long targetId) {
        if (user == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        followService.unfollow(user.getId(), targetId);
        return ResponseEntity.ok("언팔로우 했습니다.");
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserSearchResponse>> getFollowers(@LoginUser User user) {
        if (user == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(followService.getFollowers(user.getId()));
    }

    @GetMapping("/following")
    public ResponseEntity<List<UserSearchResponse>> getFollowing(@LoginUser User user) {
        if (user == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(followService.getFollowing(user.getId()));
    }
}