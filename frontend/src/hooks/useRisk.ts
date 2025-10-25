/**
 * Risk Hook
 * 단일 책임: 리스크 관리 관련 상태 및 API 호출만 담당
 * One Source of Truth: 리스크 데이터는 이 Hook을 통해서만
 */

import { useState, useEffect, useCallback } from 'react';
import {
  getRiskStatus,
  getConsecutiveLosses,
  getCircuitBreakerStatus,
  getCooldownStatus,
  validateTradeAmount,
  getRecentRiskEvents,
  getDailyLossLimitStatus,
  getRiskSettings,
  resetCircuitBreaker,
  resetCooldown,
} from '@/services/api';
import type { RiskEvent } from '@/types/api';

export const useRisk = () => {
  const [riskStatus, setRiskStatus] = useState<{
    consecutiveLosses: number;
    circuitBreakerActive: boolean;
    cooldownActive: boolean;
    dailyLossLimitReached: boolean;
  } | null>(null);
  const [consecutiveLosses, setConsecutiveLosses] = useState<number>(0);
  const [circuitBreakerActive, setCircuitBreakerActive] = useState<boolean>(false);
  const [cooldownActive, setCooldownActive] = useState<boolean>(false);
  const [cooldownRemaining, setCooldownRemaining] = useState<number>(0);
  const [recentEvents, setRecentEvents] = useState<RiskEvent[]>([]);
  const [dailyLossLimit, setDailyLossLimit] = useState<{
    isExceeded: boolean;
    currentLoss: number;
    maxLoss: number;
  } | null>(null);
  const [riskSettings, setRiskSettings] = useState<{
    maxConsecutiveLosses: number;
    circuitBreakerThreshold: number;
    cooldownDuration: number;
    maxDailyLoss: number;
  } | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 전체 리스크 상태 조회
   * 단일 책임: 리스크 상태 조회만 담당
   */
  const fetchRiskStatus = useCallback(async () => {
    try {
      const response = await getRiskStatus();
      if (response.status === 'success') {
        setRiskStatus(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('리스크 상태 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 연속 손실 횟수 조회
   * 단일 책임: 연속 손실 조회만 담당
   */
  const fetchConsecutiveLosses = useCallback(async () => {
    try {
      const response = await getConsecutiveLosses();
      if (response.status === 'success') {
        setConsecutiveLosses(response.data.count);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('연속 손실 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 서킷브레이커 상태 조회
   * 단일 책임: 서킷브레이커 상태 조회만 담당
   */
  const fetchCircuitBreakerStatus = useCallback(async () => {
    try {
      const response = await getCircuitBreakerStatus();
      if (response.status === 'success') {
        setCircuitBreakerActive(response.data.isTriggered);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('서킷브레이커 상태 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 쿨다운 상태 조회
   * 단일 책임: 쿨다운 상태 조회만 담당
   */
  const fetchCooldownStatus = useCallback(async () => {
    try {
      const response = await getCooldownStatus();
      if (response.status === 'success') {
        setCooldownActive(response.data.isActive);
        setCooldownRemaining(response.data.remainingSeconds);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('쿨다운 상태 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 최근 리스크 이벤트 조회
   * 단일 책임: 리스크 이벤트 조회만 담당
   */
  const fetchRecentEvents = useCallback(async (limit: number = 10) => {
    try {
      setLoading(true);
      const response = await getRecentRiskEvents(limit);
      if (response.status === 'success') {
        setRecentEvents(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('리스크 이벤트 조회 중 오류가 발생했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 일일 손실 한도 상태 조회
   * 단일 책임: 일일 손실 한도 조회만 담당
   */
  const fetchDailyLossLimit = useCallback(async () => {
    try {
      const response = await getDailyLossLimitStatus();
      if (response.status === 'success') {
        setDailyLossLimit(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('일일 손실 한도 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 리스크 설정 조회
   * 단일 책임: 리스크 설정 조회만 담당
   */
  const fetchRiskSettings = useCallback(async () => {
    try {
      const response = await getRiskSettings();
      if (response.status === 'success') {
        setRiskSettings(response.data);
        setError(null);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('리스크 설정 조회 중 오류가 발생했습니다.');
      console.error(err);
    }
  }, []);

  /**
   * 거래 금액 검증
   * 단일 책임: 금액 검증만 담당
   */
  const checkTradeAmount = useCallback(async (amount: number) => {
    try {
      const response = await validateTradeAmount(amount);
      if (response.status === 'success') {
        return response.data.isValid;
      } else {
        setError(response.message);
        return false;
      }
    } catch (err) {
      setError('금액 검증 중 오류가 발생했습니다.');
      console.error(err);
      return false;
    }
  }, []);

  /**
   * 서킷브레이커 리셋
   * 단일 책임: 서킷브레이커 리셋만 담당
   */
  const resetCircuitBreakerStatus = useCallback(async () => {
    try {
      setLoading(true);
      const response = await resetCircuitBreaker();
      if (response.status === 'success') {
        setCircuitBreakerActive(false);
        setError(null);
        return true;
      } else {
        setError(response.message);
        return false;
      }
    } catch (err) {
      setError('서킷브레이커 리셋 중 오류가 발생했습니다.');
      console.error(err);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 쿨다운 리셋
   * 단일 책임: 쿨다운 리셋만 담당
   */
  const resetCooldownStatus = useCallback(async () => {
    try {
      setLoading(true);
      const response = await resetCooldown();
      if (response.status === 'success') {
        setCooldownActive(false);
        setCooldownRemaining(0);
        setError(null);
        return true;
      } else {
        setError(response.message);
        return false;
      }
    } catch (err) {
      setError('쿨다운 리셋 중 오류가 발생했습니다.');
      console.error(err);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  // 초기 데이터 로드
  useEffect(() => {
    fetchRiskStatus();
    fetchConsecutiveLosses();
    fetchCircuitBreakerStatus();
    fetchCooldownStatus();
    fetchRecentEvents();
    fetchDailyLossLimit();
    fetchRiskSettings();
  }, [
    fetchRiskStatus,
    fetchConsecutiveLosses,
    fetchCircuitBreakerStatus,
    fetchCooldownStatus,
    fetchRecentEvents,
    fetchDailyLossLimit,
    fetchRiskSettings,
  ]);

  // 쿨다운 타이머 (1초마다 업데이트)
  useEffect(() => {
    if (cooldownActive && cooldownRemaining > 0) {
      const interval = setInterval(() => {
        setCooldownRemaining((prev) => Math.max(0, prev - 1));
      }, 1000);

      return () => clearInterval(interval);
    }
  }, [cooldownActive, cooldownRemaining]);

  return {
    riskStatus,
    consecutiveLosses,
    circuitBreakerActive,
    cooldownActive,
    cooldownRemaining,
    recentEvents,
    dailyLossLimit,
    riskSettings,
    loading,
    error,
    fetchRiskStatus,
    fetchConsecutiveLosses,
    fetchCircuitBreakerStatus,
    fetchCooldownStatus,
    fetchRecentEvents,
    fetchDailyLossLimit,
    fetchRiskSettings,
    checkTradeAmount,
    resetCircuitBreakerStatus,
    resetCooldownStatus,
  };
};

