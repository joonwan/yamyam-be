package com.ssafy.yamyam_coach.mapper.post;

import com.ssafy.yamyam_coach.domain.post.Post;
import com.ssafy.yamyam_coach.repository.post.request.UpdatePostRepositoryRequest;
import com.ssafy.yamyam_coach.repository.post.response.PostDetailResponse;
import com.ssafy.yamyam_coach.repository.post.response.PostInfoResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {

    int insert(Post post);

    Post findById(Long postId);

    int update(UpdatePostRepositoryRequest request);

    int deleteById(Long postId);

    PostDetailResponse findPostDetail(Long postId, Long userId);

    int incrementLikeCount(Long postId);

    Post findByIdForUpdate(Long postId);

    int decrementLikeCount(Long postId);

    List<PostInfoResponse> findPostInfos();
}
