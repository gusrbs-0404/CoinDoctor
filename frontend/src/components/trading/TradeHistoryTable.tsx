/**
 * TradeHistoryTable 컴포넌트
 * 단일 책임: 거래 내역 테이블 렌더링만 담당
 */

import React, { useEffect } from 'react';
import { useTrading } from '@/hooks';
import { Card, Loading } from '@/components/common';
import type { TradeLog } from '@/types/api';

export const TradeHistoryTable: React.FC = () => {
  const { history, loading, fetchHistory } = useTrading();

  useEffect(() => {
    fetchHistory();
  }, [fetchHistory]);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR');
  };

  const formatPrice = (price: number) => {
    return price.toLocaleString('ko-KR') + '원';
  };

  const formatProfitLoss = (profitLoss: number) => {
    const sign = profitLoss >= 0 ? '+' : '';
    const color = profitLoss >= 0 ? 'text-green-600' : 'text-red-600';
    return (
      <span className={`font-semibold ${color}`}>
        {sign}
        {profitLoss.toFixed(2)}%
      </span>
    );
  };

  return (
    <Card title="거래 내역" subtitle={`총 ${history.length}건`}>
      {loading ? (
        <Loading message="거래 내역을 불러오는 중..." />
      ) : history.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <p>거래 내역이 없습니다.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  시간
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  종목
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  타입
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  가격
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  수량
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  손익
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {history.map((trade: TradeLog) => (
                <tr key={trade.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-900">
                    {formatDate(trade.createdAt)}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm font-medium text-gray-900">
                    코인 #{trade.coinId}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap">
                    <span
                      className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        trade.tradeType === 'BUY'
                          ? 'bg-blue-100 text-blue-800'
                          : 'bg-purple-100 text-purple-800'
                      }`}
                    >
                      {trade.tradeType === 'BUY' ? '매수' : '매도'}
                    </span>
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-right text-gray-900">
                    {formatPrice(trade.price)}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-right text-gray-900">
                    {trade.quantity.toFixed(8)}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-right">
                    {trade.profitLoss !== null && formatProfitLoss(trade.profitLoss)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
};

