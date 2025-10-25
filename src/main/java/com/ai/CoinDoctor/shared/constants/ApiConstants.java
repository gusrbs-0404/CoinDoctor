package com.ai.CoinDoctor.shared.constants;

/**
 * API 관련 상수 정의
 */
public class ApiConstants {
    
    // 업비트 API 관련
    public static final String UPBIT_BASE_URL = "https://api.upbit.com";
    public static final String UPBIT_WS_URL = "wss://api.upbit.com/websocket/v1";
    public static final int UPBIT_TIMEOUT = 5000;
    public static final int UPBIT_RATE_LIMIT = 10; // 초당 요청 제한
    
    // API 엔드포인트
    public static final String UPBIT_MARKET_ALL = "/v1/market/all";
    public static final String UPBIT_TICKER = "/v1/ticker";
    public static final String UPBIT_CANDLES = "/v1/candles/minutes/1";
    public static final String UPBIT_ORDER = "/v1/orders";
    public static final String UPBIT_ACCOUNT = "/v1/accounts";
    
    // WebSocket 관련
    public static final String WS_TOPIC_MARKET = "/topic/market";
    public static final String WS_TOPIC_TRADES = "/topic/trades";
    public static final String WS_TOPIC_SYSTEM = "/topic/system";
    
    // HTTP 상태 코드
    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_TOO_MANY_REQUESTS = 429;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;
    
    // API 응답 메시지
    public static final String SUCCESS_MESSAGE = "성공";
    public static final String ERROR_MESSAGE = "오류가 발생했습니다";
    public static final String VALIDATION_ERROR_MESSAGE = "입력값이 올바르지 않습니다";
    public static final String API_ERROR_MESSAGE = "API 호출 중 오류가 발생했습니다";
    public static final String NETWORK_ERROR_MESSAGE = "네트워크 연결에 문제가 있습니다";
}
