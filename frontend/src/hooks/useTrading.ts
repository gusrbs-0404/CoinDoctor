/**
 * Trading Hook
 * 단일 책임: 거래 관련 상태 및 API 호출만 담당
 * One Source of Truth: 거래 상태는 이 Hook을 통해서만
 */

import { useState, useEffect, useCallback } from 'react';
import {
  startAutoTrading,
  stopAutoTrading,
  getTradingStatus,
  getTradingConfig,
  updateTradingConfig,
  getTradeHistory,
  getTotalProfit,
} from '@/services/api';
import type { SystemStatus, TradingConfig, TradeLog } from '@/types/api';

export const useTrading = () => {
  const [status, setStatus] = useState<SystemStatus | null>(null);
  const [config, setConfig] = useState<TradingConfig | null>(null);
  const [history, setHistory] = useState<TradeLog[]>([]);
  const [totalProfit, setTotalProfit] = useState<number>(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 자동매매 상태 조회
   * 단일 책임: 상태 조회만 담당
   */
  const fetchStatus = useCallback(async () => {
    try {
      setLoading(true);
      const response = await getTradingStatus();
      if (response.status === 'success') {
        setStatus(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('상태 조회 중 오류가 발생했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 거래 설정 조회
   * 단일 책임: 설정 조회만 담당
   */
  const fetchConfig = useCallback(async () => {
    try {
      setLoading(true);
      const response = await getTradingConfig();
      if (response.status === 'success') {
        setConfig(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('설정 조회 중 오류가 발생했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 거래 내역 조회
   * 단일 책임: 내역 조회만 담당
   */
  const fetchHistory = useCallback(async (params?: {
    startDate?: string;
    endDate?: string;
    coinId?: number;
    tradeType?: 'BUY' | 'SELL';
  }) => {
    try {
      setLoading(true);
      const response = await getTradeHistory(params);
      if (response.status === 'success') {
        setHistory(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('내역 조회 중 오류가 발생했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 총 손익 조회
   * 단일 책임: 손익 조회만 담당
   */
  const fetchTotalProfit = useCallback(async () => {
    try {
      const response = await getTotalProfit();
      if (response.status === 'success') {
        setTotalProfit(response.data.totalProfit);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('손익 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 자동매매 시작
   * 단일 책임: 시작 요청만 담당
   */
  const start = useCallback(async () => {
    try {
      setLoading(true);
      const response = await startAutoTrading();
      if (response.status === 'success') {
        setStatus(response.data);
        setError(null);
        return true;
      } else {
        setError(response.message);
        return false;
      }
    } catch (err) {
      setError('시작 중 오류가 발생했습니다.');
      console.error(err);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 자동매매 중지
   * 단일 책임: 중지 요청만 담당
   */
  const stop = useCallback(async () => {
    try {
      setLoading(true);
      const response = await stopAutoTrading();
      if (response.status === 'success') {
        setStatus(response.data);
        setError(null);
        return true;
      } else {
        setError(response.message);
        return false;
      }
    } catch (err) {
      setError('중지 중 오류가 발생했습니다.');
      console.error(err);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 거래 설정 업데이트
   * 단일 책임: 설정 업데이트만 담당
   */
  const updateConfig = useCallback(async (newConfig: Partial<TradingConfig>) => {
    try {
      setLoading(true);
      const response = await updateTradingConfig(newConfig);
      if (response.status === 'success') {
        setConfig(response.data);
        setError(null);
        return true;
      } else {
        setError(response.message);
        return false;
      }
    } catch (err) {
      setError('설정 업데이트 중 오류가 발생했습니다.');
      console.error(err);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  // 초기 데이터 로드
  useEffect(() => {
    fetchStatus();
    fetchConfig();
    fetchTotalProfit();
  }, [fetchStatus, fetchConfig, fetchTotalProfit]);

  return {
    status,
    config,
    history,
    totalProfit,
    loading,
    error,
    start,
    stop,
    updateConfig,
    fetchStatus,
    fetchConfig,
    fetchHistory,
    fetchTotalProfit,
  };
};

