package com.ssafy.yamyam_coach.service.post.request;

import lombok.Builder;
import lombok.Data;

@Data
public class UpdatePostServiceRequest {
    private Long postId;
    private Long dietPlanId;
    private String title;
    private String content;

    @Builder
    private UpdatePostServiceRequest(Long postId, Long dietPlanId, String title, String content) {
        this.postId = postId;
        this.dietPlanId = dietPlanId;
        this.title = title;
        this.content = content;
    }
}
