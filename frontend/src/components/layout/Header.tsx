/**
 * Header 컴포넌트
 * 단일 책임: 페이지 헤더 렌더링만 담당
 */

import React from 'react';

export const Header: React.FC = () => {
  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* 로고 */}
          <div className="flex items-center">
            <h1 className="text-2xl font-bold text-blue-600">💊 CoinDoctor</h1>
            <span className="ml-3 px-2 py-1 bg-blue-100 text-blue-800 text-xs font-semibold rounded">
              AI 자동매매
            </span>
          </div>

          {/* 우측 정보 */}
          <div className="flex items-center space-x-4">
            <div className="text-right">
              <p className="text-sm text-gray-600">실시간 모니터링</p>
              <p className="text-xs text-gray-400">
                {new Date().toLocaleString('ko-KR')}
              </p>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};

