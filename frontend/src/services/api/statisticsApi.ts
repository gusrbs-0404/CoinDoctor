/**
 * Statistics API 서비스
 * 단일 책임: Statistics API 호출만 담당
 * One Source of Truth: Statistics API는 이 파일을 통해서만
 */

import { apiClient } from './client';
import { API_ENDPOINTS } from '@/constants/api';
import type { ApiResponse, DailySummary } from '@/types/api';

/**
 * 특정 날짜 통계 조회
 */
export const getDailyStatistics = async (date: string): Promise<ApiResponse<DailySummary>> => {
  return apiClient.get<DailySummary>(API_ENDPOINTS.STATISTICS.DAILY(date));
};

/**
 * 오늘 통계 조회
 */
export const getTodayStatistics = async (): Promise<ApiResponse<DailySummary>> => {
  return apiClient.get<DailySummary>(API_ENDPOINTS.STATISTICS.TODAY);
};

/**
 * 기간별 통계 조회
 */
export const getRangeStatistics = async (params: {
  startDate: string;
  endDate: string;
}): Promise<ApiResponse<DailySummary[]>> => {
  return apiClient.get<DailySummary[]>(API_ENDPOINTS.STATISTICS.RANGE, params);
};

/**
 * 최근 N일 통계 조회
 */
export const getRecentStatistics = async (days: number = 7): Promise<ApiResponse<DailySummary[]>> => {
  return apiClient.get<DailySummary[]>(API_ENDPOINTS.STATISTICS.RECENT, { days });
};

/**
 * 전체 통계 요약 조회
 */
export const getTotalSummary = async (): Promise<ApiResponse<{
  totalTrades: number;
  totalProfit: number;
  averageWinRate: number;
  tradingDays: number;
}>> => {
  return apiClient.get(API_ENDPOINTS.STATISTICS.SUMMARY);
};

/**
 * 월별 통계 조회
 */
export const getMonthlyStatistics = async (year: number, month: number): Promise<ApiResponse<{
  year: number;
  month: number;
  totalTrades: number;
  totalProfit: number;
  averageWinRate: number;
  tradingDays: number;
}>> => {
  return apiClient.get(API_ENDPOINTS.STATISTICS.MONTHLY(year, month));
};

/**
 * 승률 조회
 */
export const getWinRate = async (params?: {
  startDate?: string;
  endDate?: string;
}): Promise<ApiResponse<{
  winRate: number;
  period?: string;
  startDate?: string;
  endDate?: string;
}>> => {
  return apiClient.get(API_ENDPOINTS.STATISTICS.WIN_RATE, params);
};

/**
 * 일별 요약 생성
 */
export const generateDailySummary = async (date: string): Promise<ApiResponse<DailySummary>> => {
  return apiClient.post<DailySummary>(API_ENDPOINTS.STATISTICS.GENERATE(date));
};

