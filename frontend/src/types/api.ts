/**
 * API 응답 타입 정의
 * One Source of Truth: API 타입은 이 파일을 통해서만
 */

/**
 * 공통 API 응답
 */
export interface ApiResponse<T = any> {
  status: 'success' | 'error';
  message: string;
  data: T;
  timestamp: string;
}

/**
 * 에러 응답
 */
export interface ErrorResponse {
  error: string;
  message: string;
  status?: number;
  timestamp: string;
  path?: string;
  details?: string[];
}

/**
 * 시장 데이터
 */
export interface MarketData {
  market: string;
  korean_name?: string;
  english_name?: string;
  trade_price: number;
  change_rate: number;
  acc_trade_volume_24h: number;
  acc_trade_price_24h: number;
  high_price: number;
  low_price: number;
  timestamp?: string;
}

/**
 * 캔들 데이터
 */
export interface CandleData {
  market: string;
  candle_date_time_utc: string;
  candle_date_time_kst: string;
  opening_price: number;
  high_price: number;
  low_price: number;
  trade_price: number;
  timestamp: number;
  candle_acc_trade_price: number;
  candle_acc_trade_volume: number;
}

/**
 * 거래 설정
 */
export interface TradingConfig {
  settingId?: number;
  tp: number;
  sl: number;
  risk: number;
  maxLossCount: number;
  circuitBreaker: number;
  amountPerTrade: number;
  apiKeyEncrypted?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 거래 내역
 */
export interface TradeLog {
  tradeId: number;
  coinId: number;
  summaryId?: number;
  tradeType: 'BUY' | 'SELL';
  price: number;
  quantity: number;
  profitLoss: number;
  createdAt: string;
}

/**
 * 일별 통계
 */
export interface DailySummary {
  summaryId: number;
  date: string;
  totalProfit: number;
  winRate: number;
  totalTrades: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * 리스크 상태
 */
export interface RiskStatus {
  tradingStatus: 'RUNNING' | 'STOPPED';
  consecutiveLosses: number;
  isCircuitBreakerTriggered: boolean;
  isCooldownActive: boolean;
  cooldownRemainingSeconds: number;
  statusReason?: string;
  lastUpdate?: string;
}

/**
 * 리스크 이벤트
 */
export interface RiskEvent {
  logId: number;
  date: string;
  eventType: 'CONSECUTIVE_LOSS' | 'CIRCUIT_BREAKER' | 'DAILY_LOSS_LIMIT' | 'API_ERROR' | 'NETWORK_ERROR' | 'MANUAL_STOP' | 'MANUAL_RESET' | 'SYSTEM_ERROR';
  detail: string;
  triggeredAt: string;
}

/**
 * 시스템 상태
 */
export interface SystemStatus {
  statusId: number;
  autoTrading: 'RUNNING' | 'STOPPED';
  lastUpdate: string;
  statusReason?: string;
  cooldownRemainingSeconds: number;
}

