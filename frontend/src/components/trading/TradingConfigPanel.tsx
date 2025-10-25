/**
 * TradingConfigPanel 컴포넌트
 * 단일 책임: 거래 설정 표시 및 수정만 담당
 */

import React, { useState } from 'react';
import { useTrading } from '@/hooks';
import { Button, Card, Alert } from '@/components/common';

export const TradingConfigPanel: React.FC = () => {
  const { config, loading, error, updateConfig } = useTrading();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    tp: config?.tp || 1.0,
    sl: config?.sl || 0.5,
    amountPerTrade: config?.amountPerTrade || 10000,
    maxLossCount: config?.maxLossCount || 3,
    circuitBreaker: config?.circuitBreaker || true,
  });

  const handleSave = async () => {
    const success = await updateConfig(formData);
    if (success) {
      setIsEditing(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      tp: config?.tp || 1.0,
      sl: config?.sl || 0.5,
      amountPerTrade: config?.amountPerTrade || 10000,
      maxLossCount: config?.maxLossCount || 3,
      circuitBreaker: config?.circuitBreaker || true,
    });
    setIsEditing(false);
  };

  return (
    <Card
      title="거래 설정"
      headerAction={
        !isEditing && (
          <Button size="sm" onClick={() => setIsEditing(true)}>
            ✏️ 수정
          </Button>
        )
      }
    >
      {error && <Alert type="error" message={error} className="mb-4" />}

      <div className="space-y-4">
        {/* Take Profit */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            익절 (Take Profit) %
          </label>
          {isEditing ? (
            <input
              type="number"
              step="0.1"
              value={formData.tp}
              onChange={(e) => setFormData({ ...formData, tp: parseFloat(e.target.value) })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          ) : (
            <p className="text-lg font-semibold text-green-600">+{config?.tp}%</p>
          )}
        </div>

        {/* Stop Loss */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            손절 (Stop Loss) %
          </label>
          {isEditing ? (
            <input
              type="number"
              step="0.1"
              value={formData.sl}
              onChange={(e) => setFormData({ ...formData, sl: parseFloat(e.target.value) })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          ) : (
            <p className="text-lg font-semibold text-red-600">-{config?.sl}%</p>
          )}
        </div>

        {/* 거래 금액 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            거래당 금액 (KRW)
          </label>
          {isEditing ? (
            <input
              type="number"
              step="1000"
              value={formData.amountPerTrade}
              onChange={(e) =>
                setFormData({ ...formData, amountPerTrade: parseInt(e.target.value) })
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          ) : (
            <p className="text-lg font-semibold text-gray-800">
              {config?.amountPerTrade?.toLocaleString()}원
            </p>
          )}
        </div>

        {/* 연속 손실 제한 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            연속 손실 제한 (회)
          </label>
          {isEditing ? (
            <input
              type="number"
              value={formData.maxLossCount}
              onChange={(e) =>
                setFormData({ ...formData, maxLossCount: parseInt(e.target.value) })
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          ) : (
            <p className="text-lg font-semibold text-gray-800">{config?.maxLossCount}회</p>
          )}
        </div>

        {/* 서킷브레이커 */}
        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
          <span className="text-sm font-medium text-gray-700">서킷브레이커</span>
          {isEditing ? (
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={formData.circuitBreaker}
                onChange={(e) =>
                  setFormData({ ...formData, circuitBreaker: e.target.checked })
                }
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          ) : (
            <span
              className={`px-3 py-1 rounded-full text-sm font-medium ${
                config?.circuitBreaker
                  ? 'bg-green-100 text-green-800'
                  : 'bg-gray-100 text-gray-800'
              }`}
            >
              {config?.circuitBreaker ? '활성화' : '비활성화'}
            </span>
          )}
        </div>

        {/* 버튼 */}
        {isEditing && (
          <div className="flex gap-3 pt-4">
            <Button variant="primary" onClick={handleSave} loading={loading} className="flex-1">
              💾 저장
            </Button>
            <Button variant="secondary" onClick={handleCancel} className="flex-1">
              ✖️ 취소
            </Button>
          </div>
        )}
      </div>
    </Card>
  );
};

