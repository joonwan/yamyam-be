package com.ssafy.yamyam_coach.controller.challenge;

import com.ssafy.yamyam_coach.controller.challenge.request.ChallengeCreateRequest;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.global.annotation.LoginUser;
import com.ssafy.yamyam_coach.service.challenge.ChallengeService;
import com.ssafy.yamyam_coach.service.challenge.response.ChallengeDetailResponse;
import com.ssafy.yamyam_coach.service.challenge.response.ChallengeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/my")
    public ResponseEntity<List<ChallengeResponse>> getMy(@LoginUser User user) {
        return ResponseEntity.ok(challengeService.getMyChallenges(user.getId()));
    }

    @GetMapping("/available")
    public ResponseEntity<List<ChallengeResponse>> getAvailable(@LoginUser User user) {
        return ResponseEntity.ok(challengeService.getAvailableChallenges(user.getId()));
    }

    @PostMapping
    public ResponseEntity<String> create(@LoginUser User user, @RequestBody ChallengeCreateRequest req) {
        challengeService.createChallenge(req, user.getId());
        return ResponseEntity.ok("생성 완료");
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<String> join(@LoginUser User user, @PathVariable Long id) {
        challengeService.joinChallenge(user.getId(), id);
        return ResponseEntity.ok("참여 완료");
    }

    @DeleteMapping("/{id}/quit")
    public ResponseEntity<String> quit(@LoginUser User user, @PathVariable Long id) {
        challengeService.quitChallenge(user.getId(), id);
        return ResponseEntity.ok("포기 완료");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@LoginUser User user, @PathVariable Long id) {
        challengeService.deleteChallenge(user.getId(), id);
        return ResponseEntity.ok("삭제 완료");
    }

    // 챌린지 상세 조회 (기록 포함)
    @GetMapping("/{id}/detail")
    public ResponseEntity<ChallengeDetailResponse> getChallengeDetail(
            @PathVariable Long id,
            @LoginUser User user
    ) {
        return ResponseEntity.ok(challengeService.getChallengeDetail(id, user.getId()));
    }

    // 오늘 성공 체크/취소 토글
    @PostMapping("/{id}/check")
    public ResponseEntity<Void> toggleCheck(
            @PathVariable Long id,
            @LoginUser User user
    ) {
        challengeService.toggleDailyLog(id, user.getId());
        return ResponseEntity.ok().build();
    }
}