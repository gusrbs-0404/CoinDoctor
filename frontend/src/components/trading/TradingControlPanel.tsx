/**
 * TradingControlPanel 컴포넌트
 * 단일 책임: 자동매매 시작/중지 제어만 담당
 */

import React from 'react';
import { useTrading } from '@/hooks';
import { Button, Card, Alert } from '@/components/common';

export const TradingControlPanel: React.FC = () => {
  const { status, loading, error, start, stop } = useTrading();

  const isRunning = status?.autoTrading === 'RUNNING';

  const handleToggle = async () => {
    if (isRunning) {
      await stop();
    } else {
      await start();
    }
  };

  return (
    <Card title="자동매매 제어" className="mb-6">
      {error && <Alert type="error" message={error} className="mb-4" />}

      <div className="space-y-4">
        {/* 상태 표시 */}
        <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
          <div>
            <p className="text-sm text-gray-600">현재 상태</p>
            <p className="text-lg font-semibold">
              {isRunning ? (
                <span className="text-green-600">🟢 실행 중</span>
              ) : (
                <span className="text-gray-600">⚫ 중지됨</span>
              )}
            </p>
          </div>
          {status?.statusReason && (
            <div className="text-right">
              <p className="text-sm text-gray-600">사유</p>
              <p className="text-sm font-medium text-gray-800">{status.statusReason}</p>
            </div>
          )}
        </div>

        {/* 쿨다운 표시 */}
        {status?.cooldownRemainingSeconds && status.cooldownRemainingSeconds > 0 && (
          <Alert
            type="warning"
            message={`쿨다운 중: ${status.cooldownRemainingSeconds}초 남음`}
          />
        )}

        {/* 제어 버튼 */}
        <div className="flex gap-3">
          <Button
            variant={isRunning ? 'danger' : 'success'}
            onClick={handleToggle}
            loading={loading}
            className="flex-1"
            size="lg"
          >
            {isRunning ? '⏹ 자동매매 중지' : '▶ 자동매매 시작'}
          </Button>
        </div>

        {/* 안내 메시지 */}
        <div className="text-xs text-gray-500 p-3 bg-blue-50 rounded">
          <p>💡 자동매매는 설정된 전략에 따라 자동으로 매수/매도를 수행합니다.</p>
          <p className="mt-1">⚠️ 리스크 관리 설정을 확인한 후 시작하세요.</p>
        </div>
      </div>
    </Card>
  );
};

