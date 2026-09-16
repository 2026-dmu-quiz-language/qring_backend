package com.qring.qring_backend.service.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 인터랙티브 스토리 모델 티어 설정: 티어별 모델 ID와 포인트 비용.
 *
 * 값은 application.yml 의 qring.openai.tiers.* 에서 오고, 이 클래스를 new 로 만들면 아래 기본값이 그대로 쓰인다
 * (단위 테스트·스모크 테스트용). 가격의 원본은 서버 설정이며, 프론트는 같은 값을 하드코딩한다 (팀 결정 2026-09-16 —
 * 별도 조회 API 없음).
 *
 * 설계 문서: INTERACTIVE_STORY_MODEL_TIER_DESIGN.md
 */
@Component
public class StoryModelTier {

    public static final String STANDARD = "standard";
    public static final String PREMIUM = "premium";

    /** 표시명 (프론트와 동일하게 유지). */
    public static final String STANDARD_LABEL = "기본 모델";
    public static final String PREMIUM_LABEL = "프리미엄 모델";

    @Value("${qring.openai.tiers.standard.model:${qring.openai.model:gpt-4.1-mini}}")
    private String standardModel = "gpt-4.1-mini";

    @Value("${qring.openai.tiers.premium.model:gpt-4.1}")
    private String premiumModel = "gpt-4.1";

    /** 스토리 시작 비용 (기본 모델). 2026-09-16 팀 결정으로 30 → 400. */
    @Value("${qring.openai.tiers.standard.start-cost:400}")
    private int standardStartCost = 400;

    /** 스토리 시작 비용 (프리미엄 모델) = 기본 400 + 업그레이드 150. */
    @Value("${qring.openai.tiers.premium.start-cost:550}")
    private int premiumStartCost = 550;

    /** 이어하기 1회 비용. 프리미엄 인상 여부는 팀 논의 후 결정 (보류) — 그때까지 양쪽 100. */
    @Value("${qring.openai.tiers.standard.extend-cost:100}")
    private int standardExtendCost = 100;

    @Value("${qring.openai.tiers.premium.extend-cost:100}")
    private int premiumExtendCost = 100;

    /**
     * 요청값을 티어 코드로 정규화한다. null/빈 값은 기본 모델. 그 외 알 수 없는 값은 400 으로 이어지는 예외.
     */
    public static String normalize(String requested) {
        if (requested == null || requested.isBlank()) {
            return STANDARD;
        }
        String tier = requested.trim().toLowerCase();
        if (STANDARD.equals(tier) || PREMIUM.equals(tier)) {
            return tier;
        }
        throw new IllegalArgumentException("modelTier 는 \"standard\" 또는 \"premium\" 이어야 합니다. (받은 값: " + requested + ")");
    }

    public static boolean isPremium(String tier) {
        return PREMIUM.equals(tier);
    }

    public static String labelOf(String tier) {
        return isPremium(tier) ? PREMIUM_LABEL : STANDARD_LABEL;
    }

    public String modelFor(String tier) {
        return isPremium(tier) ? premiumModel : standardModel;
    }

    public int startCostFor(String tier) {
        return isPremium(tier) ? premiumStartCost : standardStartCost;
    }

    public int extendCostFor(String tier) {
        return isPremium(tier) ? premiumExtendCost : standardExtendCost;
    }

    /** 프리미엄 업그레이드 추가분 (화면 표시용 "+150"). */
    public int upgradeCost() {
        return premiumStartCost - standardStartCost;
    }

    /** 스모크 테스트용: 모든 티어를 한 모델로 강제. */
    void overrideModels(String model) {
        this.standardModel = model;
        this.premiumModel = model;
    }
}
