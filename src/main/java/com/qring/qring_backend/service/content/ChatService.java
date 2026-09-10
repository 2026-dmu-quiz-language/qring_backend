package com.qring.qring_backend.service.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.Content;
import com.qring.qring_backend.domain.content.ContentRepository;
import com.qring.qring_backend.domain.content.UserContentUnlockRepository;
import com.qring.qring_backend.domain.quiz.QuizContent;
import com.qring.qring_backend.domain.quiz.QuizContentRepository;
import com.qring.qring_backend.domain.script.Script;
import com.qring.qring_backend.domain.script.ScriptRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.dto.content.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ScriptRepository scriptRepository;
    private final QuizContentRepository quizContentRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final UserContentUnlockRepository userContentUnlockRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponseDto getChatData(Long userId, Long contentId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Integer difficulty = user.getLevelCode();
        String language = user.getLanguage();

        // 유료 콘텐츠는 해금한 사용자만 열람 가능
        // (기존에는 검증이 없어 contentId 만 알면 미해금 콘텐츠의 대사·문제·정답을 그대로 받을 수 있었음)
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));
        int requiredPoints = content.getRequiredPoints() == null ? 0 : content.getRequiredPoints();
        if (requiredPoints > 0
                && !userContentUnlockRepository.existsByUserIdAndContentContentId(userId, contentId)) {
            throw new IllegalArgumentException("해금이 필요한 콘텐츠입니다. 먼저 해금해 주세요.");
        }

        List<Script> scripts = scriptRepository.findAllByContentId(contentId);
        List<ChatResponseDto.ScriptDto> scriptDtos = scripts.stream()
                .map(s -> new ChatResponseDto.ScriptDto(
                        s.getScriptId(),
                        s.getCharacterName(),
                        s.getScriptContent(),
                        s.getHasOptions()
                ))
                .toList();

        List<QuizContent> quizzes = quizContentRepository.findAllByContentIdAndDifficultyAndLanguage(
                contentId, difficulty, language);
        List<ChatResponseDto.QuizDto> quizDtos = quizzes.stream()
                .map(qc -> new ChatResponseDto.QuizDto(
                        qc.getQuizDetail().getQuizId(),
                        qc.getQuizDetail().getScript().getScriptId(),
                        qc.getQuizDetail().getQuizType(),
                        qc.getQuestion(),
                        shuffleOptions(qc.getOptions()),
                        qc.getCorrectAnswer(),
                        qc.getExplanation(),
                        qc.getHint()
                ))
                .toList();

        return new ChatResponseDto(scriptDtos, quizDtos);
    }

    private String shuffleOptions(String optionsJson) {
        if (optionsJson == null) return null;
        try {
            List<String> options = objectMapper.readValue(optionsJson, new TypeReference<>() {});
            Collections.shuffle(options);
            return objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            return optionsJson;
        }
    }
}