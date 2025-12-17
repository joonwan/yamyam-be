package com.ssafy.yamyam_coach.service.user;

import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.mapper.user.UserMapper;
import com.ssafy.yamyam_coach.repository.follow.FollowRepository;
import com.ssafy.yamyam_coach.repository.user.UserRepository;
import com.ssafy.yamyam_coach.service.user.request.UserCreateServiceRequest;
import com.ssafy.yamyam_coach.service.user.response.UserLoginServiceResponse;
import com.ssafy.yamyam_coach.security.JwtTokenProvider;
import com.ssafy.yamyam_coach.service.user.response.UserResponse;
import com.ssafy.yamyam_coach.service.user.response.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final FollowRepository followRepository;
    private final UserMapper userMapper;

    // 회원가입
    @Transactional
    public void signup(UserCreateServiceRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = request.toEntity(encodedPassword);
        
        userRepository.save(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public UserLoginServiceResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getEmail());

        return UserLoginServiceResponse.builder()
                .accessToken(token)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(User user) {
        // 1. DB에서 팔로워/팔로잉 숫자 세기
        int followerCount = followRepository.countFollowers(user.getId());
        int followingCount = followRepository.countFollowing(user.getId());

        // 2. UserResponse DTO로 변환해서 리턴
        return UserResponse.of(user, followerCount, followingCount);
    }

    @Transactional(readOnly = true)
    public UserResponse getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // DB에서 진짜 숫자 가져오기!
        int followerCount = followRepository.countFollowers(user.getId());
        int followingCount = followRepository.countFollowing(user.getId());

        // DTO 빌더에 숫자 넣기
        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .followers(followerCount)
                .following(followingCount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(String keyword, Long myId) {
        // 1. 키워드로 유저 검색 (Mapper 호출)
        List<User> users = userMapper.searchByKeyword(keyword);

        // 2. DTO 변환 (팔로우 여부 포함)
        return users.stream()
                .filter(user -> !user.getId().equals(myId)) // 나 자신 제외
                .map(user -> {
                    boolean isFollowing = followRepository.exists(myId, user.getId());
                    return new UserSearchResponse(user, isFollowing);
                })
                .collect(Collectors.toList());
    }
}