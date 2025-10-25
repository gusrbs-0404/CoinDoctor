package com.ai.CoinDoctor.shared.constants;

import java.math.BigDecimal;

/**
 * 리스크 관리 관련 상수 정의
 */
public class RiskConstants {
    
    // 연속 손실 제한
    public static final int DEFAULT_MAX_CONSECUTIVE_LOSSES = 3;
    public static final int MIN_MAX_CONSECUTIVE_LOSSES = 1;
    public static final int MAX_MAX_CONSECUTIVE_LOSSES = 10;
    
    // 서킷브레이커 관련
    public static final BigDecimal DEFAULT_CIRCUIT_BREAKER_THRESHOLD = new BigDecimal("-3.0");
    public static final BigDecimal MIN_CIRCUIT_BREAKER_THRESHOLD = new BigDecimal("-10.0");
    public static final BigDecimal MAX_CIRCUIT_BREAKER_THRESHOLD = new BigDecimal("-0.5");
    
    // 쿨다운 관련
    public static final long DEFAULT_COOLDOWN_DURATION = 600; // 10분 (초)
    public static final long MIN_COOLDOWN_DURATION = 60; // 1분
    public static final long MAX_COOLDOWN_DURATION = 3600; // 1시간
    
    // 일일 손실 한도
    public static final BigDecimal DEFAULT_MAX_DAILY_LOSS = new BigDecimal("-5.0");
    public static final BigDecimal MIN_MAX_DAILY_LOSS = new BigDecimal("-20.0");
    public static final BigDecimal MAX_MAX_DAILY_LOSS = new BigDecimal("-1.0");
    
    // 리스크 이벤트 타입
    public static final String RISK_EVENT_CONSECUTIVE_LOSS = "CONSECUTIVE_LOSS";
    public static final String RISK_EVENT_CIRCUIT_BREAKER = "CIRCUIT_BREAKER";
    public static final String RISK_EVENT_DAILY_LOSS_LIMIT = "DAILY_LOSS_LIMIT";
    public static final String RISK_EVENT_API_ERROR = "API_ERROR";
    public static final String RISK_EVENT_NETWORK_ERROR = "NETWORK_ERROR";
    
    // 시스템 상태
    public static final String SYSTEM_STATUS_RUNNING = "RUNNING";
    public static final String SYSTEM_STATUS_STOPPED = "STOPPED";
    public static final String SYSTEM_STATUS_COOLDOWN = "COOLDOWN";
    public static final String SYSTEM_STATUS_ERROR = "ERROR";
    
    // 중단 사유
    public static final String STOP_REASON_CONSECUTIVE_LOSS = "CONSECUTIVE_LOSS";
    public static final String STOP_REASON_CIRCUIT_BREAKER = "CIRCUIT_BREAKER";
    public static final String STOP_REASON_DAILY_LOSS_LIMIT = "DAILY_LOSS_LIMIT";
    public static final String STOP_REASON_MANUAL = "MANUAL";
    public static final String STOP_REASON_API_ERROR = "API_ERROR";
    public static final String STOP_REASON_NETWORK_ERROR = "NETWORK_ERROR";
}
