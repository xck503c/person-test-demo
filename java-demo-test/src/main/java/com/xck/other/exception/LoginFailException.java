package com.xck.other.exception;

public class LoginFailException extends BusinessException implements AutoCloseConnection {

    public LoginFailException() {
        super();
    }

    public LoginFailException(String message) {
        super(message);
    }
}