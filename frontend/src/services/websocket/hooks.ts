/**
 * WebSocket 관련 React Hooks
 * 단일 책임: WebSocket 구독 관리만 담당
 * One Source of Truth: WebSocket 구독은 이 Hook을 통해서만
 */

import { useEffect, useCallback, useState } from 'react';
import { wsClient } from './client';
import { WS_TOPICS } from '@/constants/api';
import type { MarketData, RiskEvent } from '@/types/api';

/**
 * WebSocket 연결 Hook
 * 단일 책임: WebSocket 연결 관리만 담당
 */
export const useWebSocket = () => {
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    // 연결
    wsClient.connect(
      () => {
        setIsConnected(true);
        setError(null);
      },
      (err) => {
        setIsConnected(false);
        setError(err);
      }
    );

    // 정리
    return () => {
      wsClient.disconnect();
      setIsConnected(false);
    };
  }, []);

  return { isConnected, error };
};

/**
 * 시장 데이터 구독 Hook
 * 단일 책임: 시장 데이터 구독만 담당
 * 에러 처리: 구독 실패 시 에러 반환
 */
export const useMarketData = (onMessage: (data: MarketData) => void) => {
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    // 구독
    wsClient.subscribe(
      WS_TOPICS.MARKET,
      (data) => {
        try {
          onMessage(data);
        } catch (err) {
          console.error('시장 데이터 처리 중 오류:', err);
          setError(err);
        }
      },
      (err) => {
        setError(err);
      }
    );

    // 정리
    return () => {
      wsClient.unsubscribe(WS_TOPICS.MARKET);
    };
  }, [onMessage]);

  return { error };
};

/**
 * 거래 알림 구독 Hook
 * 단일 책임: 거래 알림 구독만 담당
 */
export const useTradingNotifications = (onMessage: (data: any) => void) => {
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    wsClient.subscribe(
      WS_TOPICS.TRADING,
      (data) => {
        try {
          onMessage(data);
        } catch (err) {
          console.error('거래 알림 처리 중 오류:', err);
          setError(err);
        }
      },
      (err) => {
        setError(err);
      }
    );

    return () => {
      wsClient.unsubscribe(WS_TOPICS.TRADING);
    };
  }, [onMessage]);

  return { error };
};

/**
 * 리스크 이벤트 구독 Hook
 * 단일 책임: 리스크 이벤트 구독만 담당
 */
export const useRiskEvents = (onMessage: (data: RiskEvent) => void) => {
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    wsClient.subscribe(
      WS_TOPICS.RISK,
      (data) => {
        try {
          onMessage(data);
        } catch (err) {
          console.error('리스크 이벤트 처리 중 오류:', err);
          setError(err);
        }
      },
      (err) => {
        setError(err);
      }
    );

    return () => {
      wsClient.unsubscribe(WS_TOPICS.RISK);
    };
  }, [onMessage]);

  return { error };
};

/**
 * 통계 업데이트 구독 Hook
 * 단일 책임: 통계 업데이트 구독만 담당
 */
export const useStatisticsUpdates = (onMessage: (data: any) => void) => {
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    wsClient.subscribe(
      WS_TOPICS.STATISTICS,
      (data) => {
        try {
          onMessage(data);
        } catch (err) {
          console.error('통계 업데이트 처리 중 오류:', err);
          setError(err);
        }
      },
      (err) => {
        setError(err);
      }
    );

    return () => {
      wsClient.unsubscribe(WS_TOPICS.STATISTICS);
    };
  }, [onMessage]);

  return { error };
};

/**
 * 시스템 상태 구독 Hook
 * 단일 책임: 시스템 상태 구독만 담당
 */
export const useSystemStatus = (onMessage: (data: any) => void) => {
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    wsClient.subscribe(
      WS_TOPICS.SYSTEM,
      (data) => {
        try {
          onMessage(data);
        } catch (err) {
          console.error('시스템 상태 처리 중 오류:', err);
          setError(err);
        }
      },
      (err) => {
        setError(err);
      }
    );

    return () => {
      wsClient.unsubscribe(WS_TOPICS.SYSTEM);
    };
  }, [onMessage]);

  return { error };
};

/**
 * 메시지 전송 Hook
 * 단일 책임: 메시지 전송만 담당
 */
export const useSendMessage = () => {
  const sendMessage = useCallback((destination: string, body: any) => {
    try {
      wsClient.send(destination, body);
    } catch (error) {
      console.error('메시지 전송 중 오류:', error);
      throw error;
    }
  }, []);

  return { sendMessage };
};

