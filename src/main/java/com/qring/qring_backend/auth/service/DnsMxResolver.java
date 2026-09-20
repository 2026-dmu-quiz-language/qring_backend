package com.qring.qring_backend.auth.service;

/** 도메인의 DNS MX 레코드 조회. 구현체를 분리해 테스트에서 실제 DNS 없이 가짜 리졸버를 꽂을 수 있게 한다. */
public interface DnsMxResolver {

    enum Result {
        /** 널 MX(`0 .`)가 아닌 실제 MX 레코드가 1개 이상 있음. */
        HAS_MX,
        /** 도메인이 없거나(NXDOMAIN), MX 레코드가 없거나, 널 MX만 있음. 확정적 결과라 캐시한다. */
        NO_MX,
        /** 타임아웃·SERVFAIL·리졸버 접속 불가 등 조회 자체가 실패함. 일시적일 수 있어 캐시하지 않는다. */
        LOOKUP_FAILED
    }

    /** @param domain 소문자·punycode 로 정규화된 도메인 */
    Result lookupMx(String domain);
}
