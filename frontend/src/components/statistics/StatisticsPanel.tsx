/**
 * StatisticsPanel 컴포넌트
 * 단일 책임: 통계 요약 정보 렌더링만 담당
 */

import React from 'react';
import { useStatistics } from '@/hooks';
import { Card, Loading } from '@/components/common';

export const StatisticsPanel: React.FC = () => {
  const { todayStats, totalSummary, winRate, loading } = useStatistics();

  if (loading) {
    return (
      <Card title="통계 요약">
        <Loading message="통계 데이터를 불러오는 중..." />
      </Card>
    );
  }

  const formatProfit = (profit: number) => {
    const sign = profit >= 0 ? '+' : '';
    const color = profit >= 0 ? 'text-green-600' : 'text-red-600';
    return (
      <span className={`text-2xl font-bold ${color}`}>
        {sign}
        {profit.toLocaleString('ko-KR')}원
      </span>
    );
  };

  return (
    <Card title="통계 요약">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* 오늘 수익 */}
        <div className="bg-gradient-to-br from-blue-50 to-blue-100 p-4 rounded-lg">
          <p className="text-sm text-gray-600 mb-1">오늘 수익</p>
          {todayStats ? (
            formatProfit(todayStats.totalProfit)
          ) : (
            <p className="text-2xl font-bold text-gray-400">-</p>
          )}
          {todayStats && (
            <p className="text-xs text-gray-500 mt-1">거래 {todayStats.totalTrades}건</p>
          )}
        </div>

        {/* 총 수익 */}
        <div className="bg-gradient-to-br from-green-50 to-green-100 p-4 rounded-lg">
          <p className="text-sm text-gray-600 mb-1">총 수익</p>
          {totalSummary ? (
            formatProfit(totalSummary.totalProfit)
          ) : (
            <p className="text-2xl font-bold text-gray-400">-</p>
          )}
          {totalSummary && (
            <p className="text-xs text-gray-500 mt-1">
              총 {totalSummary.totalTrades}건 거래
            </p>
          )}
        </div>

        {/* 승률 */}
        <div className="bg-gradient-to-br from-purple-50 to-purple-100 p-4 rounded-lg">
          <p className="text-sm text-gray-600 mb-1">승률</p>
          <p className="text-2xl font-bold text-purple-600">{winRate.toFixed(1)}%</p>
          {totalSummary && (
            <p className="text-xs text-gray-500 mt-1">
              평균 승률 {totalSummary.averageWinRate.toFixed(1)}%
            </p>
          )}
        </div>

        {/* 거래 일수 */}
        <div className="bg-gradient-to-br from-orange-50 to-orange-100 p-4 rounded-lg">
          <p className="text-sm text-gray-600 mb-1">거래 일수</p>
          <p className="text-2xl font-bold text-orange-600">
            {totalSummary?.tradingDays || 0}일
          </p>
          {todayStats && (
            <p className="text-xs text-gray-500 mt-1">
              오늘 승률 {todayStats.winRate.toFixed(1)}%
            </p>
          )}
        </div>
      </div>
    </Card>
  );
};

