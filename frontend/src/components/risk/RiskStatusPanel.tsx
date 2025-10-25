/**
 * RiskStatusPanel 컴포넌트
 * 단일 책임: 리스크 상태 표시만 담당
 */

import React from 'react';
import { useRisk } from '@/hooks';
import { Card, Alert, Loading, Button } from '@/components/common';

export const RiskStatusPanel: React.FC = () => {
  const {
    consecutiveLosses,
    circuitBreakerActive,
    cooldownActive,
    cooldownRemaining,
    dailyLossLimit,
    riskSettings,
    loading,
    resetCircuitBreakerStatus,
    resetCooldownStatus,
  } = useRisk();

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}분 ${secs}초`;
  };

  return (
    <Card title="리스크 관리 상태">
      {loading ? (
        <Loading message="리스크 상태를 불러오는 중..." />
      ) : (
        <div className="space-y-4">
          {/* 연속 손실 */}
          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
            <div>
              <p className="text-sm text-gray-600">연속 손실</p>
              <p className="text-lg font-semibold text-gray-900">
                {consecutiveLosses} / {riskSettings?.maxConsecutiveLosses || 3}회
              </p>
            </div>
            {consecutiveLosses >= (riskSettings?.maxConsecutiveLosses || 3) && (
              <span className="px-3 py-1 bg-red-100 text-red-800 rounded-full text-sm font-medium">
                ⚠️ 한도 도달
              </span>
            )}
          </div>

          {/* 서킷브레이커 */}
          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
            <div>
              <p className="text-sm text-gray-600">서킷브레이커</p>
              <p className="text-lg font-semibold text-gray-900">
                {circuitBreakerActive ? '🔴 발동됨' : '🟢 정상'}
              </p>
            </div>
            {circuitBreakerActive && (
              <Button size="sm" variant="danger" onClick={resetCircuitBreakerStatus}>
                리셋
              </Button>
            )}
          </div>

          {/* 쿨다운 */}
          {cooldownActive && (
            <Alert
              type="warning"
              message={`쿨다운 중: ${formatTime(cooldownRemaining)} 남음`}
            />
          )}
          {cooldownActive && (
            <Button
              variant="secondary"
              onClick={resetCooldownStatus}
              className="w-full"
              size="sm"
            >
              쿨다운 강제 해제 (주의!)
            </Button>
          )}

          {/* 일일 손실 한도 */}
          {dailyLossLimit && (
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-600 mb-2">일일 손실 한도</p>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-lg font-semibold text-red-600">
                    {dailyLossLimit.currentLoss.toLocaleString('ko-KR')}원
                  </p>
                  <p className="text-xs text-gray-500">
                    한도: {dailyLossLimit.maxLoss.toLocaleString('ko-KR')}원
                  </p>
                </div>
                {dailyLossLimit.isExceeded && (
                  <span className="px-3 py-1 bg-red-100 text-red-800 rounded-full text-sm font-medium">
                    ⚠️ 초과
                  </span>
                )}
              </div>
              {/* 진행률 바 */}
              <div className="mt-3 w-full bg-gray-200 rounded-full h-2">
                <div
                  className={`h-2 rounded-full ${
                    dailyLossLimit.isExceeded ? 'bg-red-600' : 'bg-yellow-500'
                  }`}
                  style={{
                    width: `${Math.min(
                      (Math.abs(dailyLossLimit.currentLoss) / dailyLossLimit.maxLoss) * 100,
                      100
                    )}%`,
                  }}
                />
              </div>
            </div>
          )}

          {/* 리스크 설정 정보 */}
          {riskSettings && (
            <div className="text-xs text-gray-500 p-3 bg-blue-50 rounded">
              <p>
                💡 서킷브레이커 임계값: {riskSettings.circuitBreakerThreshold}% | 쿨다운 시간:{' '}
                {riskSettings.cooldownDuration}초
              </p>
            </div>
          )}
        </div>
      )}
    </Card>
  );
};

