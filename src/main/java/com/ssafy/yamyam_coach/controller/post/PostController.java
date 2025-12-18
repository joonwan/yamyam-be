package com.ssafy.yamyam_coach.controller.post;

import com.ssafy.yamyam_coach.controller.post.request.CreatePostRequest;
import com.ssafy.yamyam_coach.controller.post.request.UpdatePostRequest;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.global.annotation.LoginUser;
import com.ssafy.yamyam_coach.repository.post.response.PostDetailResponse;
import com.ssafy.yamyam_coach.repository.post.response.PostInfoResponse;
import com.ssafy.yamyam_coach.service.post.PostService;
import com.ssafy.yamyam_coach.service.post.request.UpdatePostServiceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Void> createPost(@LoginUser User currentUser, @RequestBody @Valid CreatePostRequest request) {
        Long currentUserId = currentUser.getId();
        Long createdPostId = postService.createPost(currentUserId, request.toServiceRequest());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPostId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<PostInfoResponse>> getPostInfos() {
        return ResponseEntity.ok(postService.getPostsDetail());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(@LoginUser User currentUser, @PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostDetail(currentUser.getId(), postId));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<Void> updatePost(@LoginUser User currentUser, @PathVariable Long postId, @RequestBody @Valid UpdatePostRequest request) {
        Long currentUserId = currentUser.getId();

        UpdatePostServiceRequest serviceRequest = UpdatePostServiceRequest.builder()
                .postId(postId)
                .title(request.getTitle())
                .content(request.getContent())
                .dietPlanId(request.getDietPlanId())
                .build();

        postService.updatePost(currentUserId, serviceRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@LoginUser User currentUser, @PathVariable Long postId) {
        Long currentUserId = currentUser.getId();

        postService.deletePost(currentUserId, postId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@LoginUser User currentUser, @PathVariable Long postId) {
        Long currentUserId = currentUser.getId();
        postService.likePost(currentUserId, postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/unlike")
    public ResponseEntity<Void> unlikePost(@LoginUser User currentUser, @PathVariable Long postId) {
        Long currentUserId = currentUser.getId();
        postService.unlikePost(currentUserId, postId);
        return ResponseEntity.ok().build();
    }

}
