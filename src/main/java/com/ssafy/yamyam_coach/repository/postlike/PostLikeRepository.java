package com.ssafy.yamyam_coach.repository.postlike;

import com.ssafy.yamyam_coach.domain.postlike.PostLike;

import java.util.Optional;

public interface PostLikeRepository {

    int insert(PostLike postLike);

    Optional<PostLike> findById(Long postLikeId);

    Optional<PostLike> findByPostAndUser(Long postId, Long userId);

    int deleteByPostAndUser(Long postId, Long userId);
}
