/**
 * Market API 서비스
 * 단일 책임: Market API 호출만 담당
 * One Source of Truth: Market API는 이 파일을 통해서만
 */

import { apiClient } from './client';
import { API_ENDPOINTS } from '@/constants/api';
import type { ApiResponse, MarketData, CandleData } from '@/types/api';

/**
 * 거래대금 상위 5종목 조회
 */
export const getTop5Markets = async (): Promise<ApiResponse<MarketData[]>> => {
  return apiClient.get<MarketData[]>(API_ENDPOINTS.MARKET.TOP5);
};

/**
 * 특정 마켓 현재가 조회
 */
export const getTicker = async (market: string): Promise<ApiResponse<MarketData>> => {
  return apiClient.get<MarketData>(API_ENDPOINTS.MARKET.TICKER(market));
};

/**
 * 1분봉 데이터 조회
 */
export const getCandles = async (market: string, count: number = 20): Promise<ApiResponse<CandleData[]>> => {
  return apiClient.get<CandleData[]>(API_ENDPOINTS.MARKET.CANDLES(market), { count });
};

/**
 * 여러 마켓 현재가 일괄 조회
 */
export const getMultipleTickers = async (markets: string): Promise<ApiResponse<MarketData[]>> => {
  return apiClient.get<MarketData[]>(API_ENDPOINTS.MARKET.TICKERS, { markets });
};

/**
 * 업비트 API 연결 테스트
 */
export const testConnection = async (): Promise<ApiResponse<{ connected: boolean; status: string; message: string }>> => {
  return apiClient.get<{ connected: boolean; status: string; message: string }>(API_ENDPOINTS.MARKET.CONNECTION_TEST);
};

/**
 * Market API 헬스 체크
 */
export const checkMarketHealth = async (): Promise<ApiResponse<{ status: string; timestamp: string }>> => {
  return apiClient.get<{ status: string; timestamp: string }>(API_ENDPOINTS.MARKET.HEALTH);
};

