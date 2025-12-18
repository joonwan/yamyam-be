package com.ssafy.yamyam_coach.repository.post.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostInfoResponse {
    private Long postId;
    private String title;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private String authorNickname;
}
