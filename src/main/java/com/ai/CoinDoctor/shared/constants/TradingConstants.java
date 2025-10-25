package com.ai.CoinDoctor.shared.constants;

import java.math.BigDecimal;

/**
 * 거래 관련 상수 정의
 */
public class TradingConstants {
    
    // 거래 금액 관련
    public static final BigDecimal DEFAULT_TRADING_AMOUNT = new BigDecimal("10000");
    public static final BigDecimal MAX_TRADING_AMOUNT = new BigDecimal("100000");
    public static final BigDecimal MIN_TRADING_AMOUNT = new BigDecimal("1000");
    
    // TP/SL 관련
    public static final BigDecimal DEFAULT_TP_PERCENT = new BigDecimal("1.0");
    public static final BigDecimal DEFAULT_SL_PERCENT = new BigDecimal("0.5");
    public static final BigDecimal MAX_TP_PERCENT = new BigDecimal("10.0");
    public static final BigDecimal MAX_SL_PERCENT = new BigDecimal("5.0");
    
    // 스캔 주기
    public static final long DEFAULT_SCAN_INTERVAL = 5000; // 5초
    public static final long MIN_SCAN_INTERVAL = 1000; // 1초
    public static final long MAX_SCAN_INTERVAL = 60000; // 1분
    
    // 거래 신호 관련
    public static final int EMA_SHORT_PERIOD = 5;
    public static final int EMA_LONG_PERIOD = 20;
    public static final int RSI_PERIOD = 14;
    public static final double RSI_OVERSOLD = 30.0;
    public static final double RSI_OVERBOUGHT = 70.0;
    
    // 거래 상태
    public static final String TRADING_STATUS_RUNNING = "RUNNING";
    public static final String TRADING_STATUS_STOPPED = "STOPPED";
    public static final String TRADING_STATUS_PAUSED = "PAUSED";
    
    // 주문 타입
    public static final String ORDER_TYPE_MARKET = "MARKET";
    public static final String ORDER_TYPE_LIMIT = "LIMIT";
    public static final String ORDER_SIDE_BUY = "BUY";
    public static final String ORDER_SIDE_SELL = "SELL";
    
    // 체결 상태
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_FILLED = "FILLED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String ORDER_STATUS_FAILED = "FAILED";
}
