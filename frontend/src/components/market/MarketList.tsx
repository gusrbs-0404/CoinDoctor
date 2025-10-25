/**
 * MarketList 컴포넌트
 * 단일 책임: 상위 5종목 리스트 렌더링만 담당
 */

import React from 'react';
import { useMarket } from '@/hooks';
import { Card, Loading } from '@/components/common';

export const MarketList: React.FC = () => {
  const { top5Markets, loading } = useMarket();

  const formatPrice = (price: number) => {
    return price.toLocaleString('ko-KR') + '원';
  };

  const formatChangeRate = (rate: number) => {
    const sign = rate >= 0 ? '+' : '';
    const color = rate >= 0 ? 'text-green-600' : 'text-red-600';
    return (
      <span className={`font-semibold ${color}`}>
        {sign}
        {rate.toFixed(2)}%
      </span>
    );
  };

  return (
    <Card title="거래대금 상위 5종목" subtitle="실시간 업데이트 (5초)">
      {loading ? (
        <Loading message="시장 데이터를 불러오는 중..." />
      ) : top5Markets.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <p>시장 데이터가 없습니다.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {top5Markets.map((market, index) => (
            <div
              key={market.market}
              className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <div className="flex items-center space-x-3">
                <span className="text-2xl font-bold text-gray-400">#{index + 1}</span>
                <div>
                  <p className="font-semibold text-gray-900">{market.market}</p>
                  <p className="text-sm text-gray-500">
                    거래대금: {(market.accTradePrice24h / 1000000).toFixed(0)}백만원
                  </p>
                </div>
              </div>
              <div className="text-right">
                <p className="text-lg font-semibold text-gray-900">
                  {formatPrice(market.tradePrice)}
                </p>
                <p className="text-sm">{formatChangeRate(market.changeRate * 100)}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
};

