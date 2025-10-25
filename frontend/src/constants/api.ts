/**
 * API 관련 상수
 * One Source of Truth: API 엔드포인트는 이 파일을 통해서만
 * 하드코딩 금지: 환경변수 사용
 */

// 하드코딩 금지: 환경변수에서 API URL 가져오기
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
export const WS_BASE_URL = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws';

/**
 * API 엔드포인트
 */
export const API_ENDPOINTS = {
  // Trading
  TRADING: {
    START: '/trading/start',
    STOP: '/trading/stop',
    STATUS: '/trading/status',
    CONFIG: '/trading/config',
    HISTORY: '/trading/history',
    TOTAL_PROFIT: '/trading/total-profit',
  },
  
  // Market
  MARKET: {
    TOP5: '/market/top5',
    TICKER: (market: string) => `/market/ticker/${market}`,
    CANDLES: (market: string) => `/market/candles/${market}`,
    TICKERS: '/market/tickers',
    CONNECTION_TEST: '/market/connection-test',
    HEALTH: '/market/health',
  },
  
  // Statistics
  STATISTICS: {
    DAILY: (date: string) => `/statistics/daily/${date}`,
    TODAY: '/statistics/daily/today',
    RANGE: '/statistics/range',
    RECENT: '/statistics/recent',
    SUMMARY: '/statistics/summary',
    MONTHLY: (year: number, month: number) => `/statistics/monthly/${year}/${month}`,
    WIN_RATE: '/statistics/win-rate',
    GENERATE: (date: string) => `/statistics/daily/generate/${date}`,
    HEALTH: '/statistics/health',
  },
  
  // Risk
  RISK: {
    STATUS: '/risk/status',
    CONSECUTIVE_LOSSES: '/risk/consecutive-losses',
    CIRCUIT_BREAKER: '/risk/circuit-breaker',
    COOLDOWN: '/risk/cooldown',
    VALIDATE_AMOUNT: '/risk/validate-amount',
    EVENTS: '/risk/events',
    RECENT_EVENTS: '/risk/events/recent',
    DAILY_LOSS_LIMIT: '/risk/daily-loss-limit',
    SETTINGS: '/risk/settings',
    RESET_CIRCUIT_BREAKER: '/risk/circuit-breaker/reset',
    RESET_COOLDOWN: '/risk/cooldown/reset',
    HEALTH: '/risk/health',
  },
} as const;

/**
 * WebSocket 토픽
 */
export const WS_TOPICS = {
  MARKET: '/topic/market',
  TRADING: '/topic/trading',
  RISK: '/topic/risk',
  STATISTICS: '/topic/statistics',
  SYSTEM: '/topic/system',
  NOTIFICATIONS: '/queue/notifications',
} as const;

