package com.emailagent.exception;

/**
 * Google OAuth 동의 화면에서 필수 권한(Gmail, Calendar)을 모두 허용하지 않았을 때 발생
 */
public class InsufficientScopeException extends RuntimeException {

    public InsufficientScopeException(String message) {
        super(message);
    }
}
