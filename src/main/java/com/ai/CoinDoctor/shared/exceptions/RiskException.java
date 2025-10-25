package com.ai.CoinDoctor.shared.exceptions;

/**
 * 리스크 관리 관련 예외
 */
public class RiskException extends RuntimeException {
    
    public RiskException(String message) {
        super(message);
    }
    
    public RiskException(String message, Throwable cause) {
        super(message, cause);
    }
}
