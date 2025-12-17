package com.ssafy.yamyam_coach.repository.postlike;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.dietplan.DietPlan;
import com.ssafy.yamyam_coach.domain.post.Post;
import com.ssafy.yamyam_coach.domain.postlike.PostLike;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.repository.diet_plan.DietPlanRepository;
import com.ssafy.yamyam_coach.repository.post.PostRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.ssafy.yamyam_coach.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class PostLikeRepositoryTest extends IntegrationTestSupport {

    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    DietPlanRepository dietPlanRepository;

    @DisplayName("post like 를 저장할 수 있다.")
    @Test
    void insert() {
        // given
        User user = createDummyUser();
        userRepository.save(user);

        DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        dietPlanRepository.insert(dietPlan);

        Post post = createDummyPost(user.getId(), dietPlan.getId());
        postRepository.insert(post);

        LocalDateTime now = LocalDateTime.now();
        PostLike postLike = PostLike.builder()
                .postId(post.getId())
                .userId(user.getId())
                .createdAt(now)
                .build();

        // when
        postLikeRepository.insert(postLike);

        // then
        PostLike findPostLike = postLikeRepository.findById(postLike.getId()).orElse(null);
        assertThat(findPostLike).isNotNull();
        assertThat(findPostLike.getId()).isEqualTo(postLike.getId());
        assertThat(findPostLike.getPostId()).isEqualTo(post.getId());
        assertThat(findPostLike.getUserId()).isEqualTo(user.getId());
        assertThat(findPostLike.getCreatedAt()).isNotNull();
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @DisplayName("pk 기반으로 post like 를 조회할 수 있다.")
        @Test
        void findById() {
            // given
            User user = createDummyUser();
            userRepository.save(user);

            DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
            dietPlanRepository.insert(dietPlan);

            Post post = createDummyPost(user.getId(), dietPlan.getId());
            postRepository.insert(post);

            PostLike postLike = PostLike.builder()
                    .postId(post.getId())
                    .userId(user.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikeRepository.insert(postLike);

            // when
            PostLike findPostLike = postLikeRepository.findById(postLike.getId()).orElse(null);

            // then
            assertThat(findPostLike).isNotNull();
            assertThat(findPostLike.getId()).isEqualTo(postLike.getId());
            assertThat(findPostLike.getPostId()).isEqualTo(post.getId());
            assertThat(findPostLike.getUserId()).isEqualTo(user.getId());
        }

        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 을 반환한다.")
        @Test
        void returnEmptyOptionalWhenNotExists() {
            // given
            Long notExistingId = 99999L;

            // when
            Optional<PostLike> findPostLikeOpt = postLikeRepository.findById(notExistingId);

            // then
            assertThat(findPostLikeOpt).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPostAndUser")
    class FindByPostAndUser {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("post id와 user id로 post like를 조회할 수 있다.")
            @Test
            void findByPostAndUser() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                Post post = createDummyPost(user.getId(), dietPlan.getId());
                postRepository.insert(post);

                PostLike postLike = PostLike.builder()
                        .postId(post.getId())
                        .userId(user.getId())
                        .createdAt(LocalDateTime.now())
                        .build();
                postLikeRepository.insert(postLike);

                // when
                PostLike findPostLike = postLikeRepository.findByPostAndUser(post.getId(), user.getId()).orElse(null);

                // then
                assertThat(findPostLike).isNotNull();
                assertThat(findPostLike.getId()).isEqualTo(postLike.getId());
                assertThat(findPostLike.getPostId()).isEqualTo(post.getId());
                assertThat(findPostLike.getUserId()).isEqualTo(user.getId());
            }

            @DisplayName("같은 post에 다른 user의 like가 있어도 정확히 조회된다.")
            @Test
            void findByPostAndUserWithMultipleLikes() {
                // given
                User user1 = createDummyUser();
                userRepository.save(user1);

                User user2 = createUser("user2", "nickname2", "user2@test.com", "password");
                userRepository.save(user2);

                DietPlan dietPlan = createDummyDietPlan(user1.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                Post post = createDummyPost(user1.getId(), dietPlan.getId());
                postRepository.insert(post);

                PostLike postLike1 = PostLike.builder()
                        .postId(post.getId())
                        .userId(user1.getId())
                        .createdAt(LocalDateTime.now())
                        .build();
                postLikeRepository.insert(postLike1);

                PostLike postLike2 = PostLike.builder()
                        .postId(post.getId())
                        .userId(user2.getId())
                        .createdAt(LocalDateTime.now())
                        .build();
                postLikeRepository.insert(postLike2);

                // when
                PostLike findPostLike1 = postLikeRepository.findByPostAndUser(post.getId(), user1.getId()).orElse(null);
                PostLike findPostLike2 = postLikeRepository.findByPostAndUser(post.getId(), user2.getId()).orElse(null);

                // then
                assertThat(findPostLike1).isNotNull();
                assertThat(findPostLike1.getId()).isEqualTo(postLike1.getId());
                assertThat(findPostLike1.getUserId()).isEqualTo(user1.getId());

                assertThat(findPostLike2).isNotNull();
                assertThat(findPostLike2.getId()).isEqualTo(postLike2.getId());
                assertThat(findPostLike2.getUserId()).isEqualTo(user2.getId());
            }

            @DisplayName("해당 post와 user의 like가 없을 경우 빈 optional 이 반환된다.")
            @Test
            void returnEmptyOptional() {
                // given
                User user = createDummyUser();
                userRepository.save(user);

                DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
                dietPlanRepository.insert(dietPlan);

                Post post = createDummyPost(user.getId(), dietPlan.getId());
                postRepository.insert(post);

                // when
                PostLike findPostLike = postLikeRepository.findByPostAndUser(post.getId(), user.getId()).orElse(null);

                // then
                assertThat(findPostLike).isNull();
            }
        }
    }

    @Nested
    @DisplayName("deleteByPostAndUser")
    class DeleteByPostAndUser {

        @DisplayName("post id와 user id로 post like를 삭제할 수 있다.")
        @Test
        void deleteByPostAndUser() {
            // given
            User user = createDummyUser();
            userRepository.save(user);

            DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
            dietPlanRepository.insert(dietPlan);

            Post post = createDummyPost(user.getId(), dietPlan.getId());
            postRepository.insert(post);

            PostLike postLike = PostLike.builder()
                    .postId(post.getId())
                    .userId(user.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikeRepository.insert(postLike);

            // when
            int deleteCount = postLikeRepository.deleteByPostAndUser(post.getId(), user.getId());
            PostLike findPostLike = postLikeRepository.findByPostAndUser(post.getId(), user.getId()).orElse(null);

            // then
            assertThat(deleteCount).isEqualTo(1);
            assertThat(findPostLike).isNull();
        }

        @DisplayName("같은 post에 다른 user의 like가 있어도 해당 user의 like만 삭제된다.")
        @Test
        void deleteOnlyTargetUserLike() {
            // given
            User user1 = createDummyUser();
            userRepository.save(user1);

            User user2 = createUser("user2", "nickname2", "user2@test.com", "password");
            userRepository.save(user2);

            DietPlan dietPlan = createDummyDietPlan(user1.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
            dietPlanRepository.insert(dietPlan);

            Post post = createDummyPost(user1.getId(), dietPlan.getId());
            postRepository.insert(post);

            PostLike postLike1 = PostLike.builder()
                    .postId(post.getId())
                    .userId(user1.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikeRepository.insert(postLike1);

            PostLike postLike2 = PostLike.builder()
                    .postId(post.getId())
                    .userId(user2.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikeRepository.insert(postLike2);

            // when
            int deleteCount = postLikeRepository.deleteByPostAndUser(post.getId(), user1.getId());
            PostLike findPostLike1 = postLikeRepository.findByPostAndUser(post.getId(), user1.getId()).orElse(null);
            PostLike findPostLike2 = postLikeRepository.findByPostAndUser(post.getId(), user2.getId()).orElse(null);

            // then
            assertThat(deleteCount).isEqualTo(1);
            assertThat(findPostLike1).isNull();
            assertThat(findPostLike2).isNotNull();
            assertThat(findPostLike2.getId()).isEqualTo(postLike2.getId());
        }

        @DisplayName("존재하지 않는 post like 삭제 시 0을 반환한다.")
        @Test
        void deleteNotExistingPostLike() {
            // given
            User user = createDummyUser();
            userRepository.save(user);

            DietPlan dietPlan = createDummyDietPlan(user.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
            dietPlanRepository.insert(dietPlan);

            Post post = createDummyPost(user.getId(), dietPlan.getId());
            postRepository.insert(post);

            // when
            int deleteCount = postLikeRepository.deleteByPostAndUser(post.getId(), user.getId());

            // then
            assertThat(deleteCount).isEqualTo(0);
        }
    }
}
