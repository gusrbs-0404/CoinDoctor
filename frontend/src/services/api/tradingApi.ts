/**
 * Trading API 서비스
 * 단일 책임: Trading API 호출만 담당
 * One Source of Truth: Trading API는 이 파일을 통해서만
 */

import { apiClient } from './client';
import { API_ENDPOINTS } from '@/constants/api';
import type { ApiResponse, TradingConfig, TradeLog, SystemStatus } from '@/types/api';

/**
 * 자동매매 시작
 */
export const startAutoTrading = async (): Promise<ApiResponse<SystemStatus>> => {
  return apiClient.post<SystemStatus>(API_ENDPOINTS.TRADING.START);
};

/**
 * 자동매매 중지
 */
export const stopAutoTrading = async (): Promise<ApiResponse<SystemStatus>> => {
  return apiClient.post<SystemStatus>(API_ENDPOINTS.TRADING.STOP);
};

/**
 * 자동매매 상태 조회
 */
export const getTradingStatus = async (): Promise<ApiResponse<SystemStatus>> => {
  return apiClient.get<SystemStatus>(API_ENDPOINTS.TRADING.STATUS);
};

/**
 * 거래 설정 조회
 */
export const getTradingConfig = async (): Promise<ApiResponse<TradingConfig>> => {
  return apiClient.get<TradingConfig>(API_ENDPOINTS.TRADING.CONFIG);
};

/**
 * 거래 설정 업데이트
 */
export const updateTradingConfig = async (config: Partial<TradingConfig>): Promise<ApiResponse<TradingConfig>> => {
  return apiClient.put<TradingConfig>(API_ENDPOINTS.TRADING.CONFIG, config);
};

/**
 * 거래 내역 조회
 */
export const getTradeHistory = async (params?: {
  startDate?: string;
  endDate?: string;
  coinId?: number;
  tradeType?: 'BUY' | 'SELL';
}): Promise<ApiResponse<TradeLog[]>> => {
  return apiClient.get<TradeLog[]>(API_ENDPOINTS.TRADING.HISTORY, params);
};

/**
 * 총 손익 조회
 */
export const getTotalProfit = async (params?: {
  startDate?: string;
  endDate?: string;
}): Promise<ApiResponse<{ totalProfit: number }>> => {
  return apiClient.get<{ totalProfit: number }>(API_ENDPOINTS.TRADING.TOTAL_PROFIT, params);
};

