package com.qring.qring_backend.service.content;

public class ContentLockedException extends RuntimeException {
    public ContentLockedException() {
        super("해금이 필요한 스토리입니다.");
    }
}