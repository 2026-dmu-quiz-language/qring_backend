package com.qring.qring_backend.service.content;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.script.Script;
import com.qring.qring_backend.domain.script.ScriptRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.dto.content.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ScriptRepository scriptRepository;
    private final QuizDetailRepository quizDetailRepository;
    private final UserRepository userRepository;

    public ChatResponseDto getChatData(Long userId, Long contentId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Integer difficulty = user.getLevelCode();

        // 스크립트 목록 조회 (챕터 순서 → 시퀀스 순서)
        List<Script> scripts = scriptRepository.findAllByContentId(contentId);
        List<ChatResponseDto.ScriptDto> scriptDtos = scripts.stream()
                .map(s -> new ChatResponseDto.ScriptDto(
                        s.getScriptId(),
                        s.getCharacterName(),
                        s.getScriptContent(),
                        s.getHasOptions()
                ))
                .toList();

        // 퀴즈 목록 조회 (유저 난이도 기준)
        List<QuizDetail> quizzes = quizDetailRepository.findAllByContentIdAndDifficulty(contentId, difficulty);
        List<ChatResponseDto.QuizDto> quizDtos = quizzes.stream()
                .map(q -> new ChatResponseDto.QuizDto(
                        q.getQuizId(),
                        q.getScript().getScriptId(),
                        q.getQuizType(),
                        q.getQuestion(),
                        q.getOptions(),
                        q.getCorrectAnswer(),
                        q.getExplanation(),
                        q.getHint()
                ))
                .toList();

        return new ChatResponseDto(scriptDtos, quizDtos);
    }
}
