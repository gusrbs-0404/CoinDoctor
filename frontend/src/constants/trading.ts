/**
 * 거래 관련 상수
 * One Source of Truth: 거래 설정값은 이 파일을 통해서만
 * 하드코딩 금지: 상수로 관리
 */

/**
 * 거래 금액 설정
 */
export const TRADING_AMOUNT = {
  DEFAULT: 10000,
  MIN: 1000,
  MAX: 100000,
  STEP: 1000,
} as const;

/**
 * TP/SL 설정
 */
export const TP_SL = {
  TP: {
    DEFAULT: 1.0,
    MIN: 0.1,
    MAX: 10.0,
    STEP: 0.1,
  },
  SL: {
    DEFAULT: 0.5,
    MIN: 0.1,
    MAX: 5.0,
    STEP: 0.1,
  },
} as const;

/**
 * 리스크 관리 설정
 */
export const RISK_SETTINGS = {
  MAX_CONSECUTIVE_LOSSES: 3,
  COOLDOWN_DURATION: 600, // 10분 (초)
  MAX_DAILY_LOSS: 50000, // 50,000원
  CIRCUIT_BREAKER_THRESHOLD: -3.0, // -3%
} as const;

/**
 * 차트 설정
 */
export const CHART_SETTINGS = {
  CANDLE_COUNT: 20,
  REFRESH_INTERVAL: 5000, // 5초
  EMA_SHORT: 5,
  EMA_LONG: 20,
  RSI_PERIOD: 14,
} as const;

/**
 * 거래 상태
 */
export const TRADING_STATUS = {
  RUNNING: 'RUNNING',
  STOPPED: 'STOPPED',
  PAUSED: 'PAUSED',
} as const;

/**
 * 거래 타입
 */
export const TRADE_TYPE = {
  BUY: 'BUY',
  SELL: 'SELL',
} as const;

