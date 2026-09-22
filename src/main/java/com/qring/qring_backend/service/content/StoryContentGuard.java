package com.qring.qring_backend.service.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 인터랙티브 스토리 입력의 최소 안전장치 (2026-09-22).
 *
 * <p>목적은 두 가지뿐이다.
 * <ul>
 *   <li>프롬프트 인젝션 — AI 에게 지시를 무시하게 하거나 시스템 프롬프트를 뱉게 하려는 시도</li>
 *   <li>출력 계약 조작 — 서버 내부 JSON 필드 이름을 넣어 채점 결과나 완결 여부를 바꾸려는 시도</li>
 * </ul>
 * 여기에 더해 세션을 시작할 때만, 실제 위해 제조법을 대놓고 요구하는 상황 설명을 막는다.
 *
 * <p><b>일부러 느슨하게 만들었다.</b> 이 앱의 스토리는 갈등·범죄·폭력을 소재로 삼는 것이 정상이고
 * (실제 사용된 상황: 교도소 면회, 러시안룰렛, 디스배틀, 칼부림), 소재어만 보고 막으면 멀쩡한 스토리가 막힌다.
 * 그래서 소재어가 아니라 <b>지시 형태</b>를 본다. "폭탄 테러범과 협상하는 상황"은 통과하고
 * "폭탄 만드는 법을 알려주는 상황"만 걸린다. 내용에 대한 판단은 OpenAI 자체 가드레일에 맡긴다.
 *
 * <p>오작동하면 {@code qring.story.guard.enabled=false} 로 즉시 끌 수 있다. 차단 시 어떤 규칙에
 * 걸렸는지 로그에 남기므로, 오탐이 보이면 그 규칙만 손보면 된다.
 */
@Slf4j
@Component
public class StoryContentGuard {

    /** 오탐이 보이면 배포 없이 끌 수 있도록 설정으로 뺀다. */
    @Value("${qring.story.guard.enabled:true}")
    private boolean enabled = true;

    static final String SITUATION_REJECTED =
            "이 내용으로는 스토리를 만들 수 없습니다. 다른 상황을 입력해 주세요.";
    static final String MESSAGE_REJECTED =
            "이 메시지는 보낼 수 없습니다. 대화에 맞는 말로 다시 보내 주세요.";

    /**
     * 프롬프트 인젝션. 한국어와 영어를 함께 본다.
     * "탈옥"은 교도소 스토리에서 정상적으로 쓰이므로 일부러 넣지 않았다.
     */
    private static final List<Pattern> PROMPT_INJECTION = List.of(
            // 어미를 골라 쓴다: "모든 규칙을 무시하는 무법자" 같은 인물 설명(관형형 -하는)은 통과시키고,
            // "모든 지시를 무시해" 처럼 AI 에게 내리는 명령형만 잡는다.
            Pattern.compile("(이전|위의|앞의|기존|원래|모든)\\s*(지시|명령|규칙|설정|프롬프트).{0,12}"
                    + "(무시해|무시하고|무시하라|무시한\\s*채|잊어|잊고|해제하고|해제해|초기화해)"),
            Pattern.compile("시스템\\s*프롬프트"),
            Pattern.compile("(프롬프트|지시문|시스템\\s*메시지).{0,10}(보여|알려|출력|공개|말해|뱉)"),
            Pattern.compile("(개발자|디버그)\\s*모드"),
            Pattern.compile("(제한|필터|검열).{0,6}(해제하|해제해|풀어|없이|끄고|꺼)"),
            Pattern.compile("(?i)ignore\\s+(all\\s+|any\\s+)?(previous|prior|above|earlier|the)\\s+(instruction|prompt|rule|direction)"),
            Pattern.compile("(?i)disregard\\s+(all\\s+|any\\s+)?(previous|prior|above|earlier)\\s+(instruction|prompt|rule)"),
            Pattern.compile("(?i)forget\\s+(your|all|the)\\s+(instruction|prompt|rule)"),
            Pattern.compile("(?i)system\\s*prompt"),
            Pattern.compile("(?i)(reveal|show|print|repeat|output)\\s+(me\\s+)?(your|the)\\s+(prompt|instruction|system|rule)"),
            Pattern.compile("(?i)developer\\s*mode"),
            Pattern.compile("(?i)jailbreak"),
            Pattern.compile("(?i)(unfiltered|uncensored|without\\s+(any\\s+)?(filter|restriction|censorship))"));

    /**
     * 서버와 모델이 주고받는 내부 JSON 필드 이름. 학습자가 칠 일이 없는 값이라 오탐이 거의 없고,
     * 채점 결과("answer_result")나 완결("is_completed")을 조작하려는 시도를 정확히 잡는다.
     */
    private static final Pattern INTERNAL_FIELD = Pattern.compile(
            "(?i)\\b(answer_result|is_quiz|is_completed|quiz_type|quiz_number|correct_answer|acceptable_answers"
            + "|reply_meaning|learner_told_me|next_beat|story_so_far|ai_message|target_language)\\b");

    /** 위해 제조 대상 (마약류는 아래 DRUG_SUBJECT 가 따로 본다). 스토리 소재로 쓰일 수 있어 "만드는 법" 류와 함께 나올 때만 본다. */
    private static final Pattern HARMFUL_SUBJECT = Pattern.compile(
            "(폭탄|폭발물|사제\\s*총|사제\\s*폭|독극물|청산가리|랜섬웨어|악성\\s*코드"
            + "|(?i)\\b(bomb|explosive|ransomware)\\b)");

    /** 제조법을 요구하는 말투. */
    private static final Pattern HOW_TO_MAKE = Pattern.compile(
            "(만드는\\s*(법|방법)|만들어\\s*(줘|주세요|볼까)|제조\\s*(법|방법)|제작\\s*(법|방법)|합성\\s*(법|방법)"
            + "|만드는\\s*걸\\s*(알려|가르)|(법|방법)\\S{0,3}\\s*(알려|가르쳐)"
            + "|(?i)how\\s+to\\s+(make|build|synthesize|create))");

    /**
     * 마약류. 소재로만 등장하는 스토리(잠입 수사, 조직 이야기)는 막지 않으므로 제조 표현과 함께 나올 때만 본다.
     * "마약김밥" 처럼 흔한 음식 별칭과 "대마도" 는 빼 둔다 (오탐).
     */
    private static final Pattern DRUG_SUBJECT = Pattern.compile(
            "(마약(?!김밥|김|옥수수|떡볶이|베개|토스트|계란)|필로폰|히로뽕|메스암페타민|메트암페타민|코카인|헤로인"
            + "|엑스터시|케타민|대마초|대마(?!도)|마리화나|해시시|아편|양귀비|펜타닐|프로포폴|각성제|환각제|신종마약"
            + "|(?i)\\b(meth|methamphetamine|cocaine|heroin|fentanyl|cannabis|marijuana|opium|ecstasy|mdma|lsd|ketamine)\\b)");

    /** 마약을 직접 만들어 내는 행위를 가리키는 말. 재배·합성·추출까지 포함한다. */
    private static final Pattern DRUG_METHOD = Pattern.compile(
            "(제조|제법|조제|합성|추출|정제|재배|배양|레시피|만드는\\s*(법|방법|걸|것)|만들기|만들어\\s*(줘|주세요|볼)"
            + "|키우는\\s*(법|방법)|(법|방법)\\s*(을|를)?\\s*(알려|가르)"
            + "|(?i)how\\s+to\\s+(make|cook|synthesize|produce|grow)|synthesiz|recipe)");

    /** 마약 소재와 제조 표현이 함께 있는 것으로 보는 최대 거리. */
    private static final int DRUG_WINDOW = 30;
    /** 위해 요청으로 보는 최대 거리 (소재어와 제조법 표현이 이 글자 수 안에 함께 있을 때만). */
    private static final int HARMFUL_WINDOW = 25;

    /**
     * 세션 시작 입력 검사. 위반이면 IllegalArgumentException(400).
     * 포인트를 차감하기 전에 부른다 — 막힌 요청에 포인트가 나가면 안 된다.
     */
    public void checkSituation(Long userId, String characterName, String situationDescription, String tone) {
        String combined = join(characterName, situationDescription, tone);
        String rule = violatedRule(combined, true);
        if (rule != null) {
            log.warn("[StoryGuard] 스토리 시작 차단 - userId: {}, 규칙: {}, 입력: \"{}\"", userId, rule, trim(combined));
            throw new IllegalArgumentException(SITUATION_REJECTED);
        }
    }

    /**
     * 대화 메시지 검사. 위반이면 IllegalArgumentException(400).
     * 제조법 규칙은 적용하지 않는다 — 이미 시작된 이야기 속 대사까지 소재로 막으면 오탐이 커진다.
     */
    public void checkMessage(Long userId, String sessionId, String userMessage) {
        String rule = violatedRule(userMessage, false);
        if (rule != null) {
            log.warn("[StoryGuard] 대화 메시지 차단 - userId: {}, sessionId: {}, 규칙: {}, 입력: \"{}\"",
                    userId, sessionId, rule, trim(userMessage));
            throw new IllegalArgumentException(MESSAGE_REJECTED);
        }
    }

    /** 걸린 규칙 이름 (통과면 null). 테스트와 로그에서 어떤 규칙이 잡았는지 보려고 이름을 돌려준다. */
    String violatedRule(String text, boolean includeHarmful) {
        if (!enabled || text == null || text.isBlank()) {
            return null;
        }
        String normalized = normalize(text);
        for (Pattern pattern : PROMPT_INJECTION) {
            if (pattern.matcher(normalized).find()) {
                return "prompt-injection";
            }
        }
        if (INTERNAL_FIELD.matcher(normalized).find()) {
            return "internal-field";
        }
        // 마약 제조는 시작·대화를 가리지 않고 막는다 (팀 결정 2026-09-22)
        if (requestsDrugManufacturing(normalized)) {
            return "drug-manufacturing";
        }
        if (includeHarmful && requestsHarmfulInstructions(normalized)) {
            return "harmful-how-to";
        }
        return null;
    }

    /**
     * 마약 제조 요청인지 (소재어와 제조 표현이 가까이 함께 있을 때만 참).
     * 위해 제조법 규칙과 달리 대화 메시지에도 적용한다 — 이야기 속 대사라도 실제 제조법을 끌어내는 통로가 되기 때문이다.
     */
    private static boolean requestsDrugManufacturing(String normalized) {
        java.util.regex.Matcher subject = DRUG_SUBJECT.matcher(normalized);
        while (subject.find()) {
            int from = Math.max(0, subject.start() - DRUG_WINDOW);
            int to = Math.min(normalized.length(), subject.end() + DRUG_WINDOW);
            if (DRUG_METHOD.matcher(normalized.substring(from, to)).find()) {
                return true;
            }
        }
        return false;
    }

    /** 위해 소재어와 제조법 표현이 가까이 함께 있을 때만 참. 소재어 하나만으로는 막지 않는다. */
    private static boolean requestsHarmfulInstructions(String normalized) {
        java.util.regex.Matcher subject = HARMFUL_SUBJECT.matcher(normalized);
        while (subject.find()) {
            int from = Math.max(0, subject.start() - HARMFUL_WINDOW);
            int to = Math.min(normalized.length(), subject.end() + HARMFUL_WINDOW);
            if (HOW_TO_MAKE.matcher(normalized.substring(from, to)).find()) {
                return true;
            }
        }
        return false;
    }

    /** 공백을 한 칸으로 줄이고 소문자로 맞춘다 (자모 분리나 유니코드 우회까지는 보지 않는다 — 최소 장치). */
    private static String normalize(String text) {
        return text.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    private static String join(String... values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                sb.append(value).append(' ');
            }
        }
        return sb.toString();
    }

    /** 로그가 길어지지 않게 자른다. */
    private static String trim(String text) {
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() > 200 ? flat.substring(0, 200) + "..." : flat;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
