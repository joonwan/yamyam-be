package com.ssafy.yamyam_coach.controller.diet_plan;

import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateDietPlanRequest;
import com.ssafy.yamyam_coach.controller.diet_plan.request.UpdateDietPlanRequest;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.global.annotation.LoginUser;
import com.ssafy.yamyam_coach.service.diet_plan.DietPlanService;
import com.ssafy.yamyam_coach.service.diet_plan.request.UpdateDietPlanServiceRequest;
import com.ssafy.yamyam_coach.service.diet_plan.response.DietPlanServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/diet-plans")
@RequiredArgsConstructor
public class DietPlanController {

    private final DietPlanService dietPlanService;

    @PostMapping
    public ResponseEntity<Void> registerDietPlan(@LoginUser User currentUser, @RequestBody @Valid CreateDietPlanRequest request) {

        log.debug("[DietPlanController.registerDietPlan]: diet plan 생성 요청: {}", request);

        Long currentUserId = currentUser.getId();

        Long createdPlanId = dietPlanService.registerDietPlan(currentUserId, request.toServiceRequest());

        log.debug("[DietPlanController.registerDietPlan]: diet plan 생성완료! id: {}", createdPlanId);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPlanId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{dietPlanId}")
    public ResponseEntity<DietPlanServiceResponse> getDietPlanById(@PathVariable Long dietPlanId) {
        return ResponseEntity.ok(dietPlanService.getDietPlanById(dietPlanId));
    }

    @PatchMapping("/{dietPlanId}")
    public ResponseEntity<Void> updateDietPlan(@LoginUser User currentUser, @PathVariable Long dietPlanId, @RequestBody UpdateDietPlanRequest request) {
        Long currentUserId = currentUser.getId();
        log.debug("[DietPlanController.updateDietPlan]: 식단 계획 업데이트 요청. target diet plan id = {} current user = {}", dietPlanId, currentUserId);

        UpdateDietPlanServiceRequest serviceRequest = UpdateDietPlanServiceRequest.builder()
                .dietPlanId(dietPlanId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .content(request.getContent())
                .build();

        dietPlanService.updateDietPlan(currentUserId, serviceRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{dietPlanId}/primary")
    public ResponseEntity<Void> changePrimaryDietPlan(@LoginUser User currentUser, @PathVariable Long dietPlanId) {
        log.debug("[DietPlanController.deleteDietPlan]: 대표 식단 변경 요청. target diet plan id = {}", dietPlanId);

        Long currentUserId = currentUser.getId();
        dietPlanService.changePrimaryDietPlanTo(currentUserId, dietPlanId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{dietPlanId}")
    public ResponseEntity<Void> deleteDietPlan(@LoginUser User currentUser, @PathVariable Long dietPlanId) {
        log.debug("[DietPlanController.deleteDietPlan]: 식단 삭제 요청. diet plan id = {}", dietPlanId);

        Long currentUserId = currentUser.getId();
        dietPlanService.deleteById(currentUserId, dietPlanId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<DietPlanServiceResponse>> getMyDietPlans(@LoginUser User currentUser) {
        Long currentUserId = currentUser.getId();
        return ResponseEntity.ok(dietPlanService.getMyDietPlans(currentUserId));
    }

    @GetMapping("/my/primary")
    public ResponseEntity<DietPlanServiceResponse> getPrimaryDietPlan(@LoginUser User currentUser) {
        Long currentUserId = currentUser.getId();
        return ResponseEntity.ok(dietPlanService.getPrimaryDietPlan(currentUserId));
    }

    @PostMapping("/{dietPlanId}/copy")
    public ResponseEntity<Void> copyDietPlan(@LoginUser User currentUser, @PathVariable Long dietPlanId) {
        log.debug("copy request!!");
        Long currentUserId = currentUser.getId();
        Long copyDietPlanId = dietPlanService.copyDietPlan(currentUserId, dietPlanId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(copyDietPlanId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

}
