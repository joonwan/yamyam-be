package com.ssafy.yamyam_coach.service.post;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.post.Post;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.exception.post.PostException;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.post.PostRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import com.ssafy.yamyam_coach.service.post.request.CreatePostServiceRequest;
import com.ssafy.yamyam_coach.service.post.request.UpdatePostServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static com.ssafy.yamyam_coach.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostServiceTest extends IntegrationTestSupport {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DietPlanRepository dietPlanRepository;

    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("dietPlanId가 null일 때 post를 정상적으로 생성한다.")
            @Test
            void createPostWithoutDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                CreatePostServiceRequest request = CreatePostServiceRequest.builder()
                        .title("게시글 제목")
                        .content("게시글 내용")
                        .dietPlanId(null)
                        .build();

                // when
                Long createdPostId = postService.createPost(user.getId(), request);

                // then
                Post findPost = postRepository.findById(createdPostId).orElse(null);
                assertThat(findPost).isNotNull();
                assertThat(findPost.getTitle()).isEqualTo("게시글 제목");
                assertThat(findPost.getContent()).isEqualTo("게시글 내용");
                assertThat(findPost.getUserId()).isEqualTo(user.getId());
                assertThat(findPost.getDietPlanId()).isNull();
                assertThat(findPost.getCopyCount()).isEqualTo(0);
                assertThat(findPost.getLikeCount()).isEqualTo(0);
            }

            @DisplayName("dietPlanId가 있을 때 dietPlan과 연결하여 post를 정상적으로 생성한다.")
            @Test
            void createPostWithDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                CreatePostServiceRequest request = CreatePostServiceRequest.builder()
                        .title("식단 공유 게시글")
                        .content("제 식단 공유합니다")
                        .dietPlanId(dietPlan.getId())
                        .build();

                // when
                Long createdPostId = postService.createPost(user.getId(), request);

                // then
                Post findPost = postRepository.findById(createdPostId).orElse(null);
                assertThat(findPost).isNotNull();
                assertThat(findPost.getTitle()).isEqualTo("식단 공유 게시글");
                assertThat(findPost.getContent()).isEqualTo("제 식단 공유합니다");
                assertThat(findPost.getUserId()).isEqualTo(user.getId());
                assertThat(findPost.getDietPlanId()).isEqualTo(dietPlan.getId());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 dietPlanId로 생성 시도 시 NOT_FOUND_DIET_PLAN 예외가 발생한다.")
            @Test
            void createPostWithNotExistingDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistingDietPlanId = 99999L;

                CreatePostServiceRequest request = CreatePostServiceRequest.builder()
                        .title("게시글 제목")
                        .content("게시글 내용")
                        .dietPlanId(notExistingDietPlanId)
                        .build();

                // when // then
                assertThatThrownBy(() -> postService.createPost(user.getId(), request))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 조회할 수 없습니다.");
            }

            @DisplayName("다른 사용자의 dietPlan으로 생성 시도 시 UNAUTHORIZED_FOR_CREATE_POST 예외가 발생한다.")
            @Test
            void createPostWithOtherUsersDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                User other = createUser("다른사람", "다른닉네임", "other@test.com", "password");
                userRepository.save(other);

                DietPlan othersDietPlan = createDummyDietPlan(other.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(othersDietPlan);

                CreatePostServiceRequest request = CreatePostServiceRequest.builder()
                        .title("게시글 제목")
                        .content("게시글 내용")
                        .dietPlanId(othersDietPlan.getId())
                        .build();

                // when // then
                assertThatThrownBy(() -> postService.createPost(user.getId(), request))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획으로 게시글을 만들 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("updatePost")
    class UpdatePost {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("title만 수정할 수 있다")
            @Test
            void updateOnlyTitle() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Post post = createDummyPost(user.getId(), null);
                postRepository.insert(post);

                String originalContent = post.getContent();

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(post.getId())
                        .title("수정된 제목")
                        .content(null)
                        .dietPlanId(null)
                        .build();

                // when
                postService.updatePost(user.getId(), request);

                // then
                Post updatedPost = postRepository.findById(post.getId()).orElse(null);
                assertThat(updatedPost).isNotNull();
                assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
                assertThat(updatedPost.getContent()).isEqualTo(originalContent);
                assertThat(updatedPost.getDietPlanId()).isNull();
            }

            @DisplayName("content만 수정할 수 있다. (Partial Update)")
            @Test
            void updateOnlyContent() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Post post = createDummyPost(user.getId(), null);
                postRepository.insert(post);

                String originalTitle = post.getTitle();

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(post.getId())
                        .title(null)
                        .content("수정된 내용")
                        .dietPlanId(null)
                        .build();

                // when
                postService.updatePost(user.getId(), request);

                // then
                Post updatedPost = postRepository.findById(post.getId()).orElse(null);
                assertThat(updatedPost).isNotNull();
                assertThat(updatedPost.getTitle()).isEqualTo(originalTitle);  // 유지됨
                assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
                assertThat(updatedPost.getDietPlanId()).isNull();  // 유지됨
            }

            @DisplayName("title과 content를 동시에 수정할 수 있다.")
            @Test
            void updateTitleAndContent() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Post post = createDummyPost(user.getId(), null);
                postRepository.insert(post);

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(post.getId())
                        .title("새로운 제목")
                        .content("새로운 내용")
                        .dietPlanId(null)
                        .build();

                // when
                postService.updatePost(user.getId(), request);

                // then
                Post updatedPost = postRepository.findById(post.getId()).orElse(null);
                assertThat(updatedPost).isNotNull();
                assertThat(updatedPost.getTitle()).isEqualTo("새로운 제목");
                assertThat(updatedPost.getContent()).isEqualTo("새로운 내용");
            }

            @DisplayName("dietPlanId를 null에서 특정 dietPlan으로 변경할 수 있다.")
            @Test
            void updateDietPlanFromNullToSpecific() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Post post = createDummyPost(user.getId(), null);
                postRepository.insert(post);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(post.getId())
                        .title(null)
                        .content(null)
                        .dietPlanId(dietPlan.getId())
                        .build();

                // when
                postService.updatePost(user.getId(), request);

                // then
                Post updatedPost = postRepository.findById(post.getId()).orElse(null);
                assertThat(updatedPost).isNotNull();
                assertThat(updatedPost.getDietPlanId()).isEqualTo(dietPlan.getId());
            }

            @DisplayName("dietPlanId를 -1로 설정하여 dietPlan 연관을 해제할 수 있다.")
            @Test
            void removeDietPlanAssociation() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                Post post = createDummyPost(user.getId(), dietPlan.getId());
                postRepository.insert(post);

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(post.getId())
                        .title(null)
                        .content(null)
                        .dietPlanId(-1L)  // -1로 설정하여 연관 해제
                        .build();

                // when
                postService.updatePost(user.getId(), request);

                // then
                Post updatedPost = postRepository.findById(post.getId()).orElse(null);
                assertThat(updatedPost).isNotNull();
                assertThat(updatedPost.getDietPlanId()).isNull();
            }

            @DisplayName("dietPlanId를 다른 dietPlan으로 변경할 수 있다.")
            @Test
            void changeDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan1 = createDietPlan(user.getId(), "title 1", "content 1", false, true, LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan1);

                DietPlan dietPlan2 = createDietPlan(user.getId(), "title 1", "content 1", false, false, LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan2);

                Post post = createDummyPost(user.getId(), dietPlan1.getId());
                postRepository.insert(post);

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(post.getId())
                        .title(null)
                        .content(null)
                        .dietPlanId(dietPlan2.getId())
                        .build();

                // when
                postService.updatePost(user.getId(), request);

                // then
                Post updatedPost = postRepository.findById(post.getId()).orElse(null);
                assertThat(updatedPost).isNotNull();
                assertThat(updatedPost.getDietPlanId()).isEqualTo(dietPlan2.getId());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 post를 수정하려 할 때 NOT_FOUND_POST 예외가 발생한다.")
            @Test
            void updateNotExistingPost() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistingPostId = 99999L;

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(notExistingPostId)
                        .title("수정된 제목")
                        .content("수정된 내용")
                        .dietPlanId(null)
                        .build();

                // when // then
                assertThatThrownBy(() -> postService.updatePost(user.getId(), request))
                        .isInstanceOf(PostException.class)
                        .hasMessage("해당 게시글을 찾을 수 없습니다.");
            }

            @DisplayName("다른 사용자의 post를 수정하려 할 때 UNAUTHORIZED_FOR_CREATE_POST 예외가 발생한다.")
            @Test
            void updateOtherUsersPost() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                User other = createUser("다른사람", "다른닉네임", "other@test.com", "password");
                userRepository.save(other);

                Post othersPost = createDummyPost(other.getId(), null);
                postRepository.insert(othersPost);

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(othersPost.getId())
                        .title("수정된 제목")
                        .content("수정된 내용")
                        .dietPlanId(null)
                        .build();

                // when // then
                assertThatThrownBy(() -> postService.updatePost(user.getId(), request))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획으로 게시글을 만들 수 없습니다.");
            }

            @DisplayName("존재하지 않는 dietPlanId로 수정 시도 시 NOT_FOUND_DIET_PLAN 예외가 발생한다.")
            @Test
            void updateWithNotExistingDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Post post = createDummyPost(user.getId(), null);
                postRepository.insert(post);

                Long notExistingDietPlanId = 99999L;

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(post.getId())
                        .title(null)
                        .content(null)
                        .dietPlanId(notExistingDietPlanId)
                        .build();

                // when // then
                assertThatThrownBy(() -> postService.updatePost(user.getId(), request))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획을 조회할 수 없습니다.");
            }

            @DisplayName("다른 사용자의 dietPlan으로 수정 시도 시 UNAUTHORIZED_FOR_CREATE_POST 예외가 발생한다.")
            @Test
            void updateWithOtherUsersDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                User other = createUser("다른사람", "다른닉네임", "other@test.com", "password");
                userRepository.save(other);

                Post post = createDummyPost(user.getId(), null);
                postRepository.insert(post);

                DietPlan othersDietPlan = createDummyDietPlan(other.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(othersDietPlan);

                UpdatePostServiceRequest request = UpdatePostServiceRequest.builder()
                        .postId(post.getId())
                        .title(null)
                        .content(null)
                        .dietPlanId(othersDietPlan.getId())
                        .build();

                // when // then
                assertThatThrownBy(() -> postService.updatePost(user.getId(), request))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획으로 게시글을 만들 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("post를 정상적으로 삭제한다.")
            @Test
            void deletePostSuccessfully() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Post post = createDummyPost(user.getId(), null);
                postRepository.insert(post);

                // when
                postService.deletePost(user.getId(), post.getId());

                // then
                Post deletedPost = postRepository.findById(post.getId()).orElse(null);
                assertThat(deletedPost).isNull();
            }

            @DisplayName("dietPlan과 연결된 post도 정상적으로 삭제한다.")
            @Test
            void deletePostWithDietPlan() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(7));
                dietPlanRepository.insert(dietPlan);

                Post post = createDummyPost(user.getId(), dietPlan.getId());
                postRepository.insert(post);

                // when
                postService.deletePost(user.getId(), post.getId());

                // then
                Post deletedPost = postRepository.findById(post.getId()).orElse(null);
                assertThat(deletedPost).isNull();
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 post를 삭제하려 할 때 NOT_FOUND_POST 예외가 발생한다.")
            @Test
            void deleteNotExistingPost() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                Long notExistingPostId = 99999L;

                // when // then
                assertThatThrownBy(() -> postService.deletePost(user.getId(), notExistingPostId))
                        .isInstanceOf(PostException.class)
                        .hasMessage("해당 게시글을 찾을 수 없습니다.");
            }

            @DisplayName("다른 사용자의 post를 삭제하려 할 때 UNAUTHORIZED_FOR_CREATE_POST 예외가 발생한다.")
            @Test
            void deleteOtherUsersPost() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                User other = createUser("다른사람", "다른닉네임", "other@test.com", "password");
                userRepository.save(other);

                Post othersPost = createDummyPost(other.getId(), null);
                postRepository.insert(othersPost);

                // when // then
                assertThatThrownBy(() -> postService.deletePost(user.getId(), othersPost.getId()))
                        .isInstanceOf(DietPlanException.class)
                        .hasMessage("해당 식단 계획으로 게시글을 만들 수 없습니다.");
            }
        }
    }
}
