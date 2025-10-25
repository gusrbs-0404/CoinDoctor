/**
 * Market Hook
 * 단일 책임: 시장 데이터 관련 상태 및 API 호출만 담당
 * One Source of Truth: 시장 데이터는 이 Hook을 통해서만
 */

import { useState, useEffect, useCallback } from 'react';
import {
  getTop5Markets,
  getTicker,
  getCandles,
  getMultipleTickers,
} from '@/services/api';
import type { MarketData, CandleData } from '@/types/api';

export const useMarket = () => {
  const [top5Markets, setTop5Markets] = useState<MarketData[]>([]);
  const [selectedMarket, setSelectedMarket] = useState<MarketData | null>(null);
  const [candles, setCandles] = useState<CandleData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 상위 5종목 조회
   * 단일 책임: 상위 종목 조회만 담당
   */
  const fetchTop5 = useCallback(async () => {
    try {
      setLoading(true);
      const response = await getTop5Markets();
      if (response.status === 'success') {
        setTop5Markets(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('상위 종목 조회 중 오류가 발생했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 특정 마켓 현재가 조회
   * 단일 책임: 현재가 조회만 담당
   */
  const fetchTicker = useCallback(async (market: string) => {
    try {
      const response = await getTicker(market);
      if (response.status === 'success') {
        setSelectedMarket(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('현재가 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 캔들 데이터 조회
   * 단일 책임: 캔들 데이터 조회만 담당
   */
  const fetchCandles = useCallback(async (market: string, count: number = 20) => {
    try {
      setLoading(true);
      const response = await getCandles(market, count);
      if (response.status === 'success') {
        setCandles(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('캔들 데이터 조회 중 오류가 발생했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 여러 마켓 현재가 조회
   * 단일 책임: 다중 현재가 조회만 담당
   */
  const fetchMultipleTickers = useCallback(async (markets: string) => {
    try {
      const response = await getMultipleTickers(markets);
      if (response.status === 'success') {
        return response.data;
      } else {
        setError(response.message);
        return [];
      }
    } catch (err) {
      setError('현재가 조회 중 오류가 발생했습니다.');
      console.error(err);
      return [];
    }
  }, []);

  // 초기 데이터 로드
  useEffect(() => {
    fetchTop5();
  }, [fetchTop5]);

  // 주기적으로 상위 5종목 업데이트 (5초마다)
  useEffect(() => {
    const interval = setInterval(() => {
      fetchTop5();
    }, 5000);

    return () => clearInterval(interval);
  }, [fetchTop5]);

  return {
    top5Markets,
    selectedMarket,
    candles,
    loading,
    error,
    fetchTop5,
    fetchTicker,
    fetchCandles,
    fetchMultipleTickers,
  };
};

