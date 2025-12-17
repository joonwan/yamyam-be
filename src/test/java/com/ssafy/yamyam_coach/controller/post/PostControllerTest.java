package com.ssafy.yamyam_coach.controller.post;

import com.ssafy.yamyam_coach.MockLoginUserArgumentResolver;
import com.ssafy.yamyam_coach.RestControllerTestSupport;
import com.ssafy.yamyam_coach.controller.post.request.CreatePostRequest;
import com.ssafy.yamyam_coach.controller.post.request.UpdatePostRequest;
import com.ssafy.yamyam_coach.exception.common.advice.GlobalRestExceptionHandler;
import com.ssafy.yamyam_coach.exception.diet_plan.DietPlanException;
import com.ssafy.yamyam_coach.exception.post.PostException;
import com.ssafy.yamyam_coach.service.post.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.ssafy.yamyam_coach.exception.diet_plan.DietPlanErrorCode.NOT_FOUND_DIET_PLAN;
import static com.ssafy.yamyam_coach.exception.diet_plan.DietPlanErrorCode.UNAUTHORIZED_FOR_POST;
import static com.ssafy.yamyam_coach.exception.post.PostErrorCode.NOT_FOUND_POST;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest extends RestControllerTestSupport {

    @MockitoBean
    PostService postService;

    @BeforeEach
    void setUp() {
        MockLoginUserArgumentResolver mockLoginUserArgumentResolver = new MockLoginUserArgumentResolver(mockUser);

        mockMvc = MockMvcBuilders.standaloneSetup(new PostController(postService))
                .setCustomArgumentResolvers(mockLoginUserArgumentResolver)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("dietPlanId 없이 post 생성 시 201 Created와 Location 헤더를 반환한다")
            void createPostWithoutDietPlan() throws Exception {
                // given
                CreatePostRequest request = new CreatePostRequest();
                request.setTitle("게시글 제목");
                request.setContent("게시글 내용");
                request.setDietPlanId(null);

                // stubbing
                given(postService.createPost(anyLong(), any()))
                        .willReturn(1L);

                // when then
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andExpect(header().string("Location", endsWith("/api/posts/1")))
                        .andExpect(jsonPath("$").doesNotExist());
            }

            @Test
            @DisplayName("dietPlanId와 함께 post 생성 시 201 Created와 Location 헤더를 반환한다")
            void createPostWithDietPlan() throws Exception {
                // given
                CreatePostRequest request = new CreatePostRequest();
                request.setTitle("식단 공유 게시글");
                request.setContent("제 식단 공유합니다");
                request.setDietPlanId(1L);

                // stubbing
                given(postService.createPost(anyLong(), any()))
                        .willReturn(2L);

                // when then
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andExpect(header().string("Location", endsWith("/api/posts/2")))
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("title이 빈 문자열일 경우 400 응답이 반환된다")
            @Test
            void titleIsBlank() throws Exception {
                // given
                CreatePostRequest request = new CreatePostRequest();
                request.setTitle("");
                request.setContent("내용");
                request.setDietPlanId(null);

                // when then
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("title이 null일 경우 400 응답이 반환된다")
            @Test
            void titleIsNull() throws Exception {
                // given
                CreatePostRequest request = new CreatePostRequest();
                request.setTitle(null);
                request.setContent("내용");
                request.setDietPlanId(null);

                // when then
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("content가 빈 문자열일 경우 400 응답이 반환된다")
            @Test
            void contentIsBlank() throws Exception {
                // given
                CreatePostRequest request = new CreatePostRequest();
                request.setTitle("제목");
                request.setContent("");
                request.setDietPlanId(null);

                // when then
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("content가 null일 경우 400 응답이 반환된다")
            @Test
            void contentIsNull() throws Exception {
                // given
                CreatePostRequest request = new CreatePostRequest();
                request.setTitle("제목");
                request.setContent(null);
                request.setDietPlanId(null);

                // when then
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.fieldErrors").isArray())
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("존재하지 않는 dietPlanId로 생성 시 404 응답이 반환된다")
            @Test
            void notFoundDietPlan() throws Exception {
                // given
                CreatePostRequest request = new CreatePostRequest();
                request.setTitle("게시글 제목");
                request.setContent("게시글 내용");
                request.setDietPlanId(999L);

                // stubbing
                given(postService.createPost(anyLong(), any()))
                        .willThrow(new DietPlanException(NOT_FOUND_DIET_PLAN));

                // when then
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("다른 사용자의 dietPlan으로 생성 시 403 응답이 반환된다")
            @Test
            void unauthorizedDietPlan() throws Exception {
                // given
                CreatePostRequest request = new CreatePostRequest();
                request.setTitle("게시글 제목");
                request.setContent("게시글 내용");
                request.setDietPlanId(1L);

                // stubbing
                given(postService.createPost(anyLong(), any()))
                        .willThrow(new DietPlanException(UNAUTHORIZED_FOR_POST));

                // when then
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획으로 게시글과 관련된 작업을 할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("updatePost")
    class UpdatePost {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 요청으로 post 수정 시 200 OK를 반환한다")
            void updatePost() throws Exception {
                // given
                Long postId = 1L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title("수정된 제목")
                        .content("수정된 내용")
                        .dietPlanId(null)
                        .build();

                // stubbing
                doNothing().when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").doesNotExist());
            }

            @Test
            @DisplayName("title만 수정 시 200 OK를 반환한다")
            void updateOnlyTitle() throws Exception {
                // given
                Long postId = 1L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title("수정된 제목")
                        .content(null)
                        .dietPlanId(null)
                        .build();

                // stubbing
                doNothing().when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").doesNotExist());
            }

            @Test
            @DisplayName("content만 수정 시 200 OK를 반환한다")
            void updateOnlyContent() throws Exception {
                // given
                Long postId = 1L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title(null)
                        .content("수정된 내용")
                        .dietPlanId(null)
                        .build();

                // stubbing
                doNothing().when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").doesNotExist());
            }

            @Test
            @DisplayName("dietPlanId를 추가하여 수정 시 200 OK를 반환한다")
            void updateWithDietPlanId() throws Exception {
                // given
                Long postId = 1L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title(null)
                        .content(null)
                        .dietPlanId(1L)
                        .build();

                // stubbing
                doNothing().when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").doesNotExist());
            }

            @Test
            @DisplayName("dietPlanId를 -1로 설정하여 연관 해제 시 200 OK를 반환한다")
            void removeDietPlanAssociation() throws Exception {
                // given
                Long postId = 1L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title(null)
                        .content(null)
                        .dietPlanId(-1L)
                        .build();

                // stubbing
                doNothing().when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 post 수정 시 404 응답이 반환된다")
            @Test
            void notFoundPost() throws Exception {
                // given
                Long postId = 999L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title("수정된 제목")
                        .content("수정된 내용")
                        .dietPlanId(null)
                        .build();

                // stubbing
                doThrow(new PostException(NOT_FOUND_POST))
                        .when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 게시글을 찾을 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("다른 사용자의 post 수정 시 403 응답이 반환된다")
            @Test
            void unauthorizedPost() throws Exception {
                // given
                Long postId = 1L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title("수정된 제목")
                        .content("수정된 내용")
                        .dietPlanId(null)
                        .build();

                // stubbing
                doThrow(new DietPlanException(UNAUTHORIZED_FOR_POST))
                        .when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획으로 게시글과 관련된 작업을 할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("존재하지 않는 dietPlanId로 수정 시 404 응답이 반환된다")
            @Test
            void notFoundDietPlan() throws Exception {
                // given
                Long postId = 1L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title(null)
                        .content(null)
                        .dietPlanId(999L)
                        .build();

                // stubbing
                doThrow(new DietPlanException(NOT_FOUND_DIET_PLAN))
                        .when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획을 조회할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("다른 사용자의 dietPlan으로 수정 시 403 응답이 반환된다")
            @Test
            void unauthorizedDietPlan() throws Exception {
                // given
                Long postId = 1L;

                UpdatePostRequest request = UpdatePostRequest.builder()
                        .title(null)
                        .content(null)
                        .dietPlanId(1L)
                        .build();

                // stubbing
                doThrow(new DietPlanException(UNAUTHORIZED_FOR_POST))
                        .when(postService).updatePost(anyLong(), any());

                // when then
                mockMvc.perform(
                                patch("/api/posts/{postId}", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획으로 게시글과 관련된 작업을 할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 postId로 삭제 시 200 OK를 반환한다")
            void deletePost() throws Exception {
                // given
                Long postId = 1L;

                // stubbing
                doNothing().when(postService).deletePost(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/posts/{postId}", postId)
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("존재하지 않는 post 삭제 시 404 응답이 반환된다")
            @Test
            void notFoundPost() throws Exception {
                // given
                Long postId = 999L;

                // stubbing
                doThrow(new PostException(NOT_FOUND_POST))
                        .when(postService).deletePost(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/posts/{postId}", postId)
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 게시글을 찾을 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @DisplayName("다른 사용자의 post 삭제 시 403 응답이 반환된다")
            @Test
            void unauthorizedPost() throws Exception {
                // given
                Long postId = 1L;

                // stubbing
                doThrow(new DietPlanException(UNAUTHORIZED_FOR_POST))
                        .when(postService).deletePost(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                delete("/api/posts/{postId}", postId)
                        )
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                        .andExpect(jsonPath("$.message").value("해당 식단 계획으로 게시글과 관련된 작업을 할 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }
        }
    }

    @Nested
    @DisplayName("likePost")
    class LikePost {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase{

            @DisplayName("좋아요 요청할 경우 200 응답이 반환된다.")
            @Test
            void likePost() throws Exception {
                // given

                Long postId = 1L;

                // stubbing
                doNothing().when(postService).likePost(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                        post("/api/posts/{postId}/like", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").doesNotExist());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @DisplayName("post 를 찾을 수 없을 경우 404 응답이 반환된다.")
            @Test
            void notFoundPost() throws Exception {
                // given
                Long notExists = 9999999L;

                // stubbing
                doThrow(new PostException(NOT_FOUND_POST))
                        .when(postService).likePost(anyLong(), anyLong());

                // when then
                mockMvc.perform(
                                post("/api/posts/{postId}/like", notExists)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                        .andExpect(jsonPath("$.message").value("해당 게시글을 찾을 수 없습니다."))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());

            }
        }
    }
}
