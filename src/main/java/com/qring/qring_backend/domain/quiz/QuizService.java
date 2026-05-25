package com.qring.qring_backend.domain.quiz;

import com.qring.qring_backend.domain.quiz.ScoreTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final ScoreTableRepository scoreTableRepository;

    /**
     * 유저의 풀이 결과를 바탕으로 최종 점수를 조회하는 메서드
     */
    public int getCalculatedScore(int difficulty, int attemptCount, boolean hintUsed) {
        
        int finalAttempt = attemptCount;
        boolean finalHintUsed = hintUsed;

        if (attemptCount > 3) {
            finalAttempt = 4;      
            finalHintUsed = false; // 탈락은 힌트 여부 상관없이 false로 조회
        }

        // score_table에서 점수 조회
        return scoreTableRepository.findScoreByDifficultyAndAttemptAndHintUsed(difficulty, finalAttempt, finalHintUsed)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 배점 기준이 없습니다."));
    }
}