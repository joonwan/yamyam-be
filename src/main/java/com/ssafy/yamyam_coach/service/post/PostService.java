package com.ssafy.yamyam_coach.service.post;

import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.post.Post;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanErrorCode;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.exception.post.PostException;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.post.PostRepository;
import com.ssafy.yamyam_coach.repository.post.request.UpdatePostRepositoryRequest;
import com.ssafy.yamyam_coach.service.post.request.CreatePostServiceRequest;
import com.ssafy.yamyam_coach.service.post.request.UpdatePostServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.ssafy.yamyam_coach.exception.post.PostErrorCode.NOT_FOUND_POST;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final DietPlanRepository dietPlanRepository;

    @Transactional
    public Long createPost(Long currentUserId, CreatePostServiceRequest request) {

        Long dietPlanId = null;

        // 1. diet plan id 기반 조회
        if (request.getDietPlanId() != null) {
            DietPlan dietPlan = dietPlanRepository.findById(request.getDietPlanId())
                    .orElseThrow(() -> new DietPlanException(DietPlanErrorCode.NOT_FOUND_DIET_PLAN));

            // 2. diet plan 이 요청자의 것인지 검증
            validateUser(currentUserId, dietPlan.getUserId());
            dietPlanId = dietPlan.getId();
        }

        // 3. post 생성
        Post post = createPost(currentUserId, request, dietPlanId);
        postRepository.insert(post);

        return post.getId();
    }

    @Transactional
    public void updatePost(Long currentUserId, UpdatePostServiceRequest request) {
        // 1. post 조회 및 존재 검증
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        // 2. post 가 current user 의 것인지 검증
        validateUser(currentUserId, post.getUserId());

        // 3. diet plan id 가 -1 이 아닐 경우 해당 diet plan 이 있는지 그리고 사용자의 diet plan 인지 검증
        //    -1 일경우 post 에 연관된 diet plan 해제 -> PostMapper.xml 참조
        if (isDietPlanUpdateRequired(request)) {
            DietPlan dietPlan = dietPlanRepository.findById(request.getDietPlanId())
                    .orElseThrow(() -> new DietPlanException(DietPlanErrorCode.NOT_FOUND_DIET_PLAN));

            validateUser(currentUserId, dietPlan.getUserId());
        }

        // 4. db 에 update query 요청
        UpdatePostRepositoryRequest repositoryRequest = UpdatePostRepositoryRequest.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postId(post.getId())
                .dietPlanId(request.getDietPlanId())
                .build();

        postRepository.update(repositoryRequest);
    }

    @Transactional
    public void deletePost(Long currentUserId, Long postId) {
        // 1. 해당 post 가 있는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        // 2. post 가 현재 요청자의 것인지 확인
        validateUser(currentUserId, post.getUserId());

        // 3. 삭제
        postRepository.deleteById(postId);
    }

    public Object getPostDetail() {
        return null;
    }

    private void validateUser(Long currentUserId, Long userId) {
        if (!currentUserId.equals(userId)) {
            throw new DietPlanException(DietPlanErrorCode.UNAUTHORIZED_FOR_CREATE_POST);
        }
    }

    private static Post createPost(Long currentUserId, CreatePostServiceRequest request, Long dietPlanId) {
        return Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .dietPlanId(dietPlanId)
                .userId(currentUserId)
                .copyCount(0)
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static boolean isDietPlanUpdateRequired(UpdatePostServiceRequest request) {
        return request.getDietPlanId() != null && request.getDietPlanId() != -1;
    }
}
