package com.ssafy.yamyam_coach.service.challenge;

import com.ssafy.yamyam_coach.controller.challenge.request.ChallengeCreateRequest;
import com.ssafy.yamyam_coach.domain.challenge.Challenge;
import com.ssafy.yamyam_coach.domain.challenge.ChallengeDailyLog; // ★ 추가됨
import com.ssafy.yamyam_coach.domain.challenge_participation.ChallengeParticipation;
import com.ssafy.yamyam_coach.mapper.challenge.ChallengeMapper; // ★ 추가됨
import com.ssafy.yamyam_coach.repository.challenge.ChallengeRepository;
import com.ssafy.yamyam_coach.service.challenge.response.ChallengeDetailResponse; // ★ 추가됨
import com.ssafy.yamyam_coach.service.challenge.response.ChallengeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // ★ 추가됨
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMapper challengeMapper; // ★ 이 줄이 꼭 있어야 아래에서 빨간줄 안 떠!

    // 생성
    @Transactional
    public void createChallenge(ChallengeCreateRequest request, Long userId) {
        Challenge challenge = request.toEntity(userId);
        challengeRepository.saveChallenge(challenge);
    }

    // 조회
    @Transactional(readOnly = true)
    public List<ChallengeResponse> getMyChallenges(Long userId) {
        List<ChallengeResponse> list = challengeRepository.findMyChallenges(userId);
        list.forEach(ChallengeResponse::calculateProgress);
        return list;
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getAvailableChallenges(Long userId) {
        return challengeRepository.findAvailableChallenges(userId);
    }

    // 참여
    @Transactional
    public void joinChallenge(Long userId, Long challengeId) {
        if (challengeRepository.existsParticipation(userId, challengeId)) {
            throw new IllegalArgumentException("이미 참여 중입니다.");
        }
        ChallengeParticipation p = ChallengeParticipation.builder()
                .userId(userId).challengeId(challengeId).status("PROGRESS").created_at(LocalDateTime.now()).build();
        challengeRepository.saveParticipation(p);
    }

    // 포기
    @Transactional
    public void quitChallenge(Long userId, Long challengeId) {
        challengeRepository.updateParticipationStatus(userId, challengeId, "STOPPED");
    }

    // 삭제
    @Transactional
    public void deleteChallenge(Long userId, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId);
        if (challenge == null) throw new IllegalArgumentException("챌린지가 없습니다.");

        if (!challenge.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        challengeRepository.softDeleteChallenge(challengeId);
    }

    // [1] 디테일 정보 조회 (+ 진행률 자동 계산)
    @Transactional(readOnly = true)
    public ChallengeDetailResponse getChallengeDetail(Long challengeId, Long userId) {
        // 1. 챌린지 기본 정보 (Mapper 사용)
        Challenge challenge = challengeMapper.findById(challengeId);
        if (challenge == null) throw new IllegalArgumentException("챌린지가 없습니다.");

        // 2. 내가 성공한 날짜들 가져오기
        List<String> successDates = challengeMapper.findLogDates(challengeId, userId);

        // 3. 진행률 계산 로직
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
                challenge.getStartDate().toLocalDate(),
                challenge.getEndDate().toLocalDate()
        ) + 1;

        int successCount = successDates.size();

        int progress = (totalDays > 0) ? (int) ((double) successCount / totalDays * 100) : 0;
        if (progress > 100) progress = 100;

        return ChallengeDetailResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .progress(progress)
                .successDates(successDates)
                .build();
    }

    // [2] 오늘 성공 체크 토글
    @Transactional
    public void toggleDailyLog(Long challengeId, Long userId) {
        LocalDate today = LocalDate.now();

        if (challengeMapper.existsLog(challengeId, userId, today)) {
            challengeMapper.deleteDailyLog(challengeId, userId, today);
        } else {
            ChallengeDailyLog log = ChallengeDailyLog.builder()
                    .userId(userId)
                    .challengeId(challengeId)
                    .logDate(today)
                    .build();
            challengeMapper.saveDailyLog(log);
        }
    }
}