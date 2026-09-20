package com.qring.qring_backend.service.user;

import lombok.Getter;

/**
 * 포인트 잔액 부족. IllegalArgumentException 이라 GlobalExceptionHandler 에서 400 으로 나간다.
 * 메시지는 기존 응답과 같은 "포인트가 부족합니다." 를 유지하고, 필요/보유 금액은 필드로 따로 든다.
 */
@Getter
public class InsufficientPointsException extends IllegalArgumentException {

    private final int required;
    private final int current;

    public InsufficientPointsException(int required, int current) {
        super("포인트가 부족합니다.");
        this.required = required;
        this.current = current;
    }
}
