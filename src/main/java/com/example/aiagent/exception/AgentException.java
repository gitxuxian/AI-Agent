package com.example.aiagent.exception;

public class AgentException extends RuntimeException {


    private ErrorCode errorCode;

    public AgentException(String message) {
        super(message);
    }


    public AgentException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
