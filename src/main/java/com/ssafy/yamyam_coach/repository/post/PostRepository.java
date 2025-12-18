package com.ssafy.yamyam_coach.repository.post;

import com.ssafy.yamyam_coach.domain.post.Post;
import com.ssafy.yamyam_coach.repository.post.request.UpdatePostRepositoryRequest;
import com.ssafy.yamyam_coach.repository.post.response.PostDetailResponse;
import com.ssafy.yamyam_coach.repository.post.response.PostInfoResponse;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    int insert(Post post);

    Optional<Post> findById(Long postId);

    int update(UpdatePostRepositoryRequest request);

    int deleteById(Long postId);

    Optional<PostDetailResponse> findPostDetail(Long postId, Long userId);

    int incrementLikeCount(Long postId);

    Optional<Post> findByIdForUpdate(Long postId);

    int decrementLikeCount(Long postId);

    List<PostInfoResponse> findPostInfos();
}
