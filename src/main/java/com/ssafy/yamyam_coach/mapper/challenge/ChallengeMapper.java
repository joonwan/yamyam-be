package com.ssafy.yamyam_coach.mapper.challenge;

import com.ssafy.yamyam_coach.domain.challenge.Challenge;
import com.ssafy.yamyam_coach.domain.challenge.ChallengeDailyLog;
import com.ssafy.yamyam_coach.domain.challenge_participation.ChallengeParticipation;
import com.ssafy.yamyam_coach.service.challenge.response.ChallengeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChallengeMapper {
    // 생성
    void saveChallenge(Challenge challenge);

    // 조회
    List<ChallengeResponse> findMyChallenges(Long userId);
    List<ChallengeResponse> findAvailableChallenges(Long userId);
    Challenge findById(Long id); // 소유권 확인용

    // 참여/포기
    void saveParticipation(ChallengeParticipation participation);
    void updateParticipationStatus(@Param("userId") Long userId, @Param("challengeId") Long challengeId, @Param("status") String status);
    boolean existsParticipation(@Param("userId") Long userId, @Param("challengeId") Long challengeId);

    // 삭제 (소프트)
    void softDeleteChallenge(Long challengeId);

    // [추가] 특정 유저가 이 챌린지에서 성공한 날짜들 조회
    List<String> findLogDates(@Param("challengeId") Long challengeId, @Param("userId") Long userId);

    // [추가] 오늘 성공 체크 (저장)
    void saveDailyLog(ChallengeDailyLog log);

    // [추가] 오늘 성공 취소 (삭제)
    void deleteDailyLog(@Param("challengeId") Long challengeId, @Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    // [추가] 오늘 이미 체크했는지 확인
    boolean existsLog(@Param("challengeId") Long challengeId, @Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    ChallengeParticipation findHistory(@Param("userId") Long userId, @Param("challengeId") Long challengeId);
}