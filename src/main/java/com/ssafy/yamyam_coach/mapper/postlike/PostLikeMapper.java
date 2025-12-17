package com.ssafy.yamyam_coach.mapper.postlike;

import com.ssafy.yamyam_coach.domain.postlike.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper {
    int insert(PostLike postLike);

    PostLike findById(Long postLikeId);

    PostLike findByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);

    int deleteByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);
}
