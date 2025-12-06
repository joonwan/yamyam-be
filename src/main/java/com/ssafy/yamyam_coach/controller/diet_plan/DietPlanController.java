package com.ssafy.yamyam_coach.controller.diet_plan;

import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateDietPlanRequest;
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
        Long createdPlanId = dietPlanService.registerDietPlan(request.toServiceRequest());

        log.debug("[DietPlanController.registerDietPlan]: diet plan 생성완료! id: {}", createdPlanId);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPlanId)
                .toUri();

        return ResponseEntity
                .created(location)
                .build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<DietPlanServiceResponse>> getMyDietPlans() {
        return ResponseEntity.ok(dietPlanService.getMyDietPlans());
    }
}
