package com.ssafy.yamyam_coach.repository.postlike;

import com.ssafy.yamyam_coach.domain.postlike.PostLike;
import com.ssafy.yamyam_coach.mapper.postlike.PostLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisPostLikeRepository implements PostLikeRepository {

    private final PostLikeMapper postLikeMapper;

    @Override
    public int insert(PostLike postLike) {
        return postLikeMapper.insert(postLike);
    }

    @Override
    public Optional<PostLike> findById(Long postLikeId) {
        return Optional.ofNullable(postLikeMapper.findById(postLikeId));
    }

    @Override
    public Optional<PostLike> findByPostAndUser(Long postId, Long userId) {
        return Optional.ofNullable(postLikeMapper.findByPostAndUser(postId, userId));
    }

    @Override
    public int deleteByPostAndUser(Long postId, Long userId) {
        return postLikeMapper.deleteByPostAndUser(postId, userId);
    }
}
