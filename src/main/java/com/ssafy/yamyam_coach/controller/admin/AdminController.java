package com.ssafy.yamyam_coach.controller.admin;

import com.ssafy.yamyam_coach.service.food.FoodVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final FoodVectorService foodVectorService;

    // 이 메서드는 ROLE_ADMIN을 가진 토큰으로만 호출 가능
    @GetMapping("/users")
    public ResponseEntity<String> getAllUsers() {
        return ResponseEntity.ok("관리자님 환영합니다! 모든 유저 목록입니다.");
    }

    @PostMapping("/foods/sync")
    public ResponseEntity<Void> syncFoods() {
        log.debug("start food sync");
        foodVectorService.syncAllFoods();
        log.debug("end food sync");

        return ResponseEntity.ok().build();
    }
}