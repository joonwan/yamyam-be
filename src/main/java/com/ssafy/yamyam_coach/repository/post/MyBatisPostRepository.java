package com.ssafy.yamyam_coach.repository.post;

import com.ssafy.yamyam_coach.domain.post.Post;
import com.ssafy.yamyam_coach.mapper.post.PostMapper;
import com.ssafy.yamyam_coach.repository.post.request.UpdatePostRepositoryRequest;
import com.ssafy.yamyam_coach.repository.post.response.PostDetailResponse;
import com.ssafy.yamyam_coach.repository.post.response.PostInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisPostRepository implements PostRepository {

    private final PostMapper postMapper;

    @Override
    public int insert(Post post) {
        return postMapper.insert(post);
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return Optional.ofNullable(postMapper.findById(postId));
    }

    @Override
    public int update(UpdatePostRepositoryRequest request) {
        return postMapper.update(request);
    }

    @Override
    public int deleteById(Long postId) {
        return postMapper.deleteById(postId);
    }

    @Override
    public Optional<PostDetailResponse> findPostDetail(Long postId, Long userId) {
        return Optional.ofNullable(postMapper.findPostDetail(postId, userId));
    }

    @Override
    public int incrementLikeCount(Long postId) {
        return postMapper.incrementLikeCount(postId);
    }

    @Override
    public Optional<Post> findByIdForUpdate(Long postId) {
        return Optional.ofNullable(postMapper.findByIdForUpdate(postId));
    }

    @Override
    public int decrementLikeCount(Long postId) {
        return postMapper.decrementLikeCount(postId);
    }

    @Override
    public List<PostInfoResponse> findPostInfos() {
        return postMapper.findPostInfos();
    }
}
