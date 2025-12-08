package com.ssafy.yamyam_coach.controller.diet_plan;

import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateDailyDietRequest;
import com.ssafy.yamyam_coach.controller.diet_plan.request.CreateDietPlanRequest;
import com.ssafy.yamyam_coach.service.daily_diet.DailyDietService;
import com.ssafy.yamyam_coach.service.daily_diet.request.CreateDailyDietServiceRequest;
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

    @GetMapping("/{dietPlanId}")
    public ResponseEntity<DietPlanServiceResponse> getDietPlanById(@PathVariable Long dietPlanId) {
        return ResponseEntity.ok(dietPlanService.getDietPlanById(dietPlanId));
    }

    @PostMapping("/{dietPlanId}/daily-diets")
    public ResponseEntity<Void> registerDailyDiet(@PathVariable Long dietPlanId, @RequestBody @Valid CreateDailyDietRequest request) {

        log.debug("[DietPlanController.registerDailyDiet]: 특정 일 식단 등록 요청. request= {}", request);

        CreateDailyDietServiceRequest serviceRequest = request.toServiceRequest();
        serviceRequest.setDietPlanId(dietPlanId);

        dailyDietService.registerDailyDiet(serviceRequest);

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{dietPlanId}/daily-diets")
    public ResponseEntity<DailyDietDetailServiceResponse> getDailyDietByIdAndDate(@PathVariable Long dietPlanId, @RequestParam LocalDate date) {
        log.debug("[DietPlanController.getDailyDietByIdAndDate]: {}일 식단 조회 요청. diet plan id = {}", date, dietPlanId);
        return ResponseEntity.ok(dailyDietService.findByDietPlanIdAndDate(dietPlanId, date));
    }
}
