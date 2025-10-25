/**
 * Statistics Hook
 * 단일 책임: 통계 데이터 관련 상태 및 API 호출만 담당
 * One Source of Truth: 통계 데이터는 이 Hook을 통해서만
 */

import { useState, useEffect, useCallback } from 'react';
import {
  getTodayStatistics,
  getRecentStatistics,
  getTotalSummary,
  getWinRate,
} from '@/services/api';
import type { DailySummary } from '@/types/api';

export const useStatistics = () => {
  const [todayStats, setTodayStats] = useState<DailySummary | null>(null);
  const [recentStats, setRecentStats] = useState<DailySummary[]>([]);
  const [totalSummary, setTotalSummary] = useState<{
    totalTrades: number;
    totalProfit: number;
    averageWinRate: number;
    tradingDays: number;
  } | null>(null);
  const [winRate, setWinRate] = useState<number>(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 오늘 통계 조회
   * 단일 책임: 오늘 통계 조회만 담당
   */
  const fetchTodayStats = useCallback(async () => {
    try {
      const response = await getTodayStatistics();
      if (response.status === 'success') {
        setTodayStats(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('오늘 통계 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 최근 N일 통계 조회
   * 단일 책임: 최근 통계 조회만 담당
   */
  const fetchRecentStats = useCallback(async (days: number = 7) => {
    try {
      setLoading(true);
      const response = await getRecentStatistics(days);
      if (response.status === 'success') {
        setRecentStats(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('최근 통계 조회 중 오류가 발생했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 전체 통계 요약 조회
   * 단일 책임: 전체 요약 조회만 담당
   */
  const fetchTotalSummary = useCallback(async () => {
    try {
      const response = await getTotalSummary();
      if (response.status === 'success') {
        setTotalSummary(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('전체 통계 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 승률 조회
   * 단일 책임: 승률 조회만 담당
   */
  const fetchWinRate = useCallback(async () => {
    try {
      const response = await getWinRate();
      if (response.status === 'success') {
        setWinRate(response.data.winRate);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('승률 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  // 초기 데이터 로드
  useEffect(() => {
    fetchTodayStats();
    fetchRecentStats();
    fetchTotalSummary();
    fetchWinRate();
  }, [fetchTodayStats, fetchRecentStats, fetchTotalSummary, fetchWinRate]);

  return {
    todayStats,
    recentStats,
    totalSummary,
    winRate,
    loading,
    error,
    fetchTodayStats,
    fetchRecentStats,
    fetchTotalSummary,
    fetchWinRate,
  };
};

