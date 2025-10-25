/**
 * 메인 대시보드 페이지
 * 단일 책임: 전체 레이아웃 구성만 담당
 */

'use client';

import {
  MainLayout,
  StatisticsPanel,
  TradingControlPanel,
  TradingConfigPanel,
  MarketList,
  TradeHistoryTable,
  RiskStatusPanel,
} from '@/components';

export default function Home() {
  return (
    <MainLayout>
      {/* 상단: 통계 요약 */}
      <div className="mb-6">
        <StatisticsPanel />
      </div>

      {/* 메인 콘텐츠: 2열 레이아웃 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 좌측: 제어 및 설정 (1/3) */}
        <div className="lg:col-span-1 space-y-6">
          <TradingControlPanel />
          <TradingConfigPanel />
          <RiskStatusPanel />
        </div>

        {/* 우측: 시장 및 거래 내역 (2/3) */}
        <div className="lg:col-span-2 space-y-6">
          <MarketList />
          <TradeHistoryTable />
        </div>
      </div>
    </MainLayout>
  );
}
