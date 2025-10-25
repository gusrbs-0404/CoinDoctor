package com.ai.CoinDoctor.shared.exceptions;

/**
 * API 관련 예외
 */
public class ApiException extends RuntimeException {
    
    private final int statusCode;
    
    public ApiException(String message) {
        super(message);
        this.statusCode = 500;
    }
    
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }
    
    public ApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
