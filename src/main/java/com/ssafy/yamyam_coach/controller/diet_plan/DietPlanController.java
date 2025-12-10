package com.ssafy.yamyam_coach.controller.diet_plan;

import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateDietPlanRequest;
import com.ssafy.yamyam_coach.service.daily_diet.DailyDietService;
import com.ssafy.yamyam_coach.service.diet_plan.DietPlanService;
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
    public ResponseEntity<Void> registerDietPlan(@RequestBody @Valid CreateDietPlanRequest request) {

        log.debug("[DietPlanController.registerDietPlan]: diet plan 생성 요청: {}", request);

        /** todo 추후 jwt 에서 꺼내오도록 변경 예정 */
        Long createdPlanId = dietPlanService.registerDietPlan(1L, request.toServiceRequest());

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
    public ResponseEntity<Void> changePrimaryDietPlan(@PathVariable Long dietPlanId) {
        log.debug("[DietPlanController.deleteDietPlan]: 대표 식단 변경 요청. target diet plan id = {}", dietPlanId);
        dietPlanService.changePrimaryDietPlanTo(1L, dietPlanId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{dietPlanId}")
    public ResponseEntity<Void> deleteDietPlan(@PathVariable Long dietPlanId) {
        log.debug("[DietPlanController.deleteDietPlan]: 식단 삭제 요청. diet plan id = {}", dietPlanId);
        dietPlanService.deleteById(1L, dietPlanId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<DietPlanServiceResponse>> getMyDietPlans() {
        return ResponseEntity.ok(dietPlanService.getMyDietPlans(1L));
    }

    @GetMapping("/my/primary")
    public ResponseEntity<DietPlanServiceResponse> getPrimaryDietPlan() {
        return ResponseEntity.ok(dietPlanService.getPrimaryDietPlan(1L));
    }

}
