package com.ai.CoinDoctor.shared.exceptions;

/**
 * 거래 관련 예외
 */
public class TradingException extends RuntimeException {
    
    public TradingException(String message) {
        super(message);
    }
    
    public TradingException(String message, Throwable cause) {
        super(message, cause);
    }
}
