package com.ssafy.yamyam_coach.controller.diet_plan;

import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateOrUpdateDailyDietRequest;
import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateDietPlanRequest;
import com.ssafy.yamyam_coach.service.daily_diet.DailyDietService;
import com.ssafy.yamyam_coach.service.daily_diet.request.CreateOrUpdateDailyDietServiceRequest;
import com.ssafy.yamyam_coach.service.diet_plan.DietPlanService;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietDetailServiceResponse;
import com.ssafy.yamyam_coach.service.diet_plan.response.DietPlanServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/diet-plans")
@RequiredArgsConstructor
public class DietPlanController {

    private final DietPlanService dietPlanService;
    private final DailyDietService dailyDietService;

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

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<DietPlanServiceResponse>> getMyDietPlans() {
        return ResponseEntity.ok(dietPlanService.getMyDietPlans());
    }

    @GetMapping("/my/primary")
    public ResponseEntity<DietPlanServiceResponse> getPrimaryDietPlan() {
        return ResponseEntity.ok(dietPlanService.getPrimaryDietPlan());
    }

    @GetMapping("/{dietPlanId}")
    public ResponseEntity<DietPlanServiceResponse> getDietPlanById(@PathVariable Long dietPlanId) {
        return ResponseEntity.ok(dietPlanService.getDietPlanById(dietPlanId));
    }

    @PatchMapping("/{dietPlanId}")
    public ResponseEntity<Void> changePrimaryDietPlan(@PathVariable Long dietPlanId) {
        log.debug("[DietPlanController.deleteDietPlan]: 대표 식단 변경 요청. target diet plan id = {}", dietPlanId);
        dietPlanService.changePrimaryDietPlanTo(dietPlanId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{dietPlanId}")
    public ResponseEntity<Void> deleteDietPlan(@PathVariable Long dietPlanId) {
        log.debug("[DietPlanController.deleteDietPlan]: 식단 삭제 요청. diet plan id = {}", dietPlanId);
        dietPlanService.deleteById(dietPlanId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{dietPlanId}/daily-diets")
    public ResponseEntity<Void> registerDailyDiet(@PathVariable Long dietPlanId, @RequestBody @Valid CreateOrUpdateDailyDietRequest request) {

        log.debug("[DietPlanController.registerDailyDiet]: 특정 일 식단 등록 요청. request= {}", request);

        CreateOrUpdateDailyDietServiceRequest serviceRequest = request.toServiceRequest();
        serviceRequest.setDietPlanId(dietPlanId);

        dailyDietService.registerDailyDiet(serviceRequest);

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{dietPlanId}/daily-diets")
    public ResponseEntity<DailyDietDetailServiceResponse> getDailyDietByDietPlanIdAndDate(@PathVariable Long dietPlanId, @RequestParam LocalDate date) {
        log.debug("[DietPlanController.getDailyDietByIdAndDate]: {}일 식단 조회 요청. diet plan id = {}", date, dietPlanId);
        return ResponseEntity.ok(dailyDietService.findByDietPlanIdAndDate(dietPlanId, date));
    }

    @PatchMapping("/{dietPlanId}/daily-diets")
    public ResponseEntity<Void> updateDailyDietByDietPlanIdAndDate(@PathVariable Long dietPlanId, @RequestBody @Valid CreateOrUpdateDailyDietRequest request) {
        log.debug("[DietPlanController.updateDailyDietByDietPlanIdAndDate]: {}일 식단 업데이트 요청. diet plan id = {}", request, dietPlanId);

        CreateOrUpdateDailyDietServiceRequest serviceRequest = request.toServiceRequest();
        serviceRequest.setDietPlanId(dietPlanId);
        dailyDietService.updateDailyDiet(serviceRequest);

        return ResponseEntity.ok().build();
    }

}
