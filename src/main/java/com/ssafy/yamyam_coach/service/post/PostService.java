package com.ssafy.yamyam_coach.service.post;

import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.mealfood.MealFood;
import com.ssafy.yamyam_coach.domain.meals.Meal;
import com.ssafy.yamyam_coach.domain.post.Post;
import com.ssafy.yamyam_coach.domain.postlike.PostLike;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.exception.post.PostException;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.repository.daily_diet.response.DailyDietDetail;
import com.ssafy.yamyam_coach.repository.daily_diet.response.MealDetail;
import com.ssafy.yamyam_coach.repository.daily_diet.response.MealFoodDetail;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.meal.MealRepository;
import com.ssafy.yamyam_coach.repository.mealfood.MealFoodRepository;
import com.ssafy.yamyam_coach.repository.post.PostRepository;
import com.ssafy.yamyam_coach.repository.post.request.UpdatePostRepositoryRequest;
import com.ssafy.yamyam_coach.repository.post.response.PostDetailResponse;
import com.ssafy.yamyam_coach.repository.post.response.PostInfoResponse;
import com.ssafy.yamyam_coach.repository.postlike.PostLikeRepository;
import com.ssafy.yamyam_coach.service.diet_plan.DietPlanService;
import com.ssafy.yamyam_coach.service.diet_plan.request.CreateDietPlanServiceRequest;
import com.ssafy.yamyam_coach.service.post.request.CreatePostServiceRequest;
import com.ssafy.yamyam_coach.service.post.request.UpdatePostServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ssafy.yamyam_coach.exception.diet_plan.DietPlanErrorCode.*;
import static com.ssafy.yamyam_coach.exception.post.PostErrorCode.NOT_FOUND_POST;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final DietPlanRepository dietPlanRepository;
    private final DietPlanService dietPlanService;
    private final DailyDietRepository dailyDietRepository;
    private final MealRepository mealRepository;
    private final MealFoodRepository mealFoodRepository;

    @Transactional
    public Long createPost(Long currentUserId, CreatePostServiceRequest request) {

        Long dietPlanId = null;

        // 1. diet plan id 기반 조회
        if (request.getDietPlanId() != null) {
            DietPlan dietPlan = dietPlanRepository.findById(request.getDietPlanId())
                    .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

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
                    .orElseThrow(() -> new DietPlanException(NOT_FOUND_DIET_PLAN));

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

    @Transactional
    public void likePost(Long currentUserId, Long postId) {

        // 1. 해당 post 가 있는지 조회 후 존재 검증
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        // 2. 해당 post 에 이미 좋아요를 했는지 확인 -> 해당 post id 와 currentUserId 를 가진 post like 가 있는지 확인
        Optional<PostLike> postLikeOpt = postLikeRepository.findByPostAndUser(post.getId(), currentUserId);

        // 3. 이미 좋아요 한 이력이 있다면 return
        if (postLikeOpt.isPresent()) {
            return ;
        }

        // 4. 없다면 post like 생성
        PostLike postLike = createPostLike(currentUserId, postId);

        // 5. post like 저장
        postLikeRepository.insert(postLike);

        // 6. post 의 like count + 1  -> db level 에서 원자적으로 해야 동시성 문제 발생 안함
        postRepository.incrementLikeCount(postId);
    }

    @Transactional
    public void unlikePost(Long currentUserId, Long postId) {
        // 1. 해당 post 가 있는지 조회 후 존재 검증
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        // 2. 해당 post 에 이미 좋아요를 했는지 확인 -> 해당 post id 와 currentUserId 를 가진 post like 가 있는지 확인
        Optional<PostLike> postLikeOpt = postLikeRepository.findByPostAndUser(post.getId(), currentUserId);

        // 3. 좋아요 한 이력이 없을 경우 return
        if (postLikeOpt.isEmpty()) {
            return ;
        }

        // 4. post like 삭제
        postLikeRepository.deleteByPostAndUser(postId, currentUserId);

        // 6. post 의 like count - 1  -> db level 에서 원자적으로 해야 동시성 문제 발생 안함
        postRepository.decrementLikeCount(postId);
    }



    public PostDetailResponse getPostDetail(Long currentUserId, Long postId) {
       return postRepository.findPostDetail(postId, currentUserId)
               .orElseThrow(() -> new PostException(NOT_FOUND_POST));
    }

    public List<PostInfoResponse> getPostsDetail() {
        return postRepository.findPostInfos();
    }

    private void validateUser(Long currentUserId, Long userId) {
        if (!currentUserId.equals(userId)) {
            throw new DietPlanException(UNAUTHORIZED_FOR_POST);
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

    private static PostLike createPostLike(Long currentUserId, Long postId) {
        return PostLike.builder()
                .postId(postId)
                .userId(currentUserId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
