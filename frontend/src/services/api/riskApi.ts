/**
 * Risk API 서비스
 * 단일 책임: Risk API 호출만 담당
 * One Source of Truth: Risk API는 이 파일을 통해서만
 */

import { apiClient } from './client';
import { API_ENDPOINTS } from '@/constants/api';
import type { ApiResponse, RiskStatus, RiskEvent } from '@/types/api';

/**
 * 현재 리스크 상태 조회
 */
export const getRiskStatus = async (): Promise<ApiResponse<RiskStatus>> => {
  return apiClient.get<RiskStatus>(API_ENDPOINTS.RISK.STATUS);
};

/**
 * 연속 손실 횟수 조회
 */
export const getConsecutiveLosses = async (): Promise<ApiResponse<{
  consecutiveLosses: number;
  isLimitReached: boolean;
  maxAllowed: number;
}>> => {
  return apiClient.get(API_ENDPOINTS.RISK.CONSECUTIVE_LOSSES);
};

/**
 * 서킷브레이커 상태 조회
 */
export const getCircuitBreakerStatus = async (): Promise<ApiResponse<{
  isTriggered: boolean;
  status: string;
  message: string;
}>> => {
  return apiClient.get(API_ENDPOINTS.RISK.CIRCUIT_BREAKER);
};

/**
 * 쿨다운 타이머 상태 조회
 */
export const getCooldownStatus = async (): Promise<ApiResponse<{
  isActive: boolean;
  remainingSeconds: number;
  remainingMinutes: number;
  status: string;
  message: string;
}>> => {
  return apiClient.get(API_ENDPOINTS.RISK.COOLDOWN);
};

/**
 * 거래 금액 검증
 */
export const validateTradeAmount = async (amount: number): Promise<ApiResponse<{
  isValid: boolean;
  amount: number;
  maxAmount: number;
  message: string;
}>> => {
  return apiClient.get(API_ENDPOINTS.RISK.VALIDATE_AMOUNT, { amount });
};

/**
 * 리스크 이벤트 로그 조회
 */
export const getRiskEvents = async (params?: {
  startDate?: string;
  endDate?: string;
  eventType?: string;
}): Promise<ApiResponse<RiskEvent[]>> => {
  return apiClient.get<RiskEvent[]>(API_ENDPOINTS.RISK.EVENTS, params);
};

/**
 * 최근 리스크 이벤트 조회
 */
export const getRecentRiskEvents = async (limit: number = 10): Promise<ApiResponse<RiskEvent[]>> => {
  return apiClient.get<RiskEvent[]>(API_ENDPOINTS.RISK.RECENT_EVENTS, { limit });
};

/**
 * 일일 손실 한도 체크
 */
export const checkDailyLossLimit = async (): Promise<ApiResponse<{
  isExceeded: boolean;
  dailyLoss: number;
  maxDailyLoss: number;
  remainingAllowance: number;
  usagePercent: number;
  status: string;
  message: string;
}>> => {
  return apiClient.get(API_ENDPOINTS.RISK.DAILY_LOSS_LIMIT);
};

/**
 * 리스크 설정 조회
 */
export const getRiskSettings = async (): Promise<ApiResponse<{
  maxConsecutiveLosses: number;
  maxTradeAmount: number;
  maxDailyLoss: number;
  cooldownDurationSeconds: number;
  circuitBreakerThreshold: number;
}>> => {
  return apiClient.get(API_ENDPOINTS.RISK.SETTINGS);
};

/**
 * 서킷브레이커 수동 해제
 */
export const resetCircuitBreaker = async (): Promise<ApiResponse<{
  status: string;
  message: string;
  timestamp: string;
}>> => {
  return apiClient.post(API_ENDPOINTS.RISK.RESET_CIRCUIT_BREAKER);
};

/**
 * 쿨다운 타이머 수동 해제
 */
export const resetCooldown = async (): Promise<ApiResponse<{
  status: string;
  message: string;
  timestamp: string;
}>> => {
  return apiClient.post(API_ENDPOINTS.RISK.RESET_COOLDOWN);
};

