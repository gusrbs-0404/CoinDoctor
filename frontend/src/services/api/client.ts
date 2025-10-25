/**
 * API 클라이언트
 * 단일 책임: HTTP 요청 처리만 담당
 * One Source of Truth: API 호출은 이 클라이언트를 통해서만
 */

import { API_BASE_URL } from '@/constants/api';
import type { ApiResponse, ErrorResponse } from '@/types/api';

/**
 * API 클라이언트 클래스
 */
class ApiClient {
  private baseURL: string;

  constructor(baseURL: string = API_BASE_URL) {
    this.baseURL = baseURL;
  }

  /**
   * GET 요청
   * 단일 책임: GET 요청만 담당
   * 에러 처리: try-catch로 예외 처리
   */
  async get<T>(endpoint: string, params?: Record<string, any>): Promise<ApiResponse<T>> {
    try {
      // 하드코딩 금지: params를 URLSearchParams로 변환
      const url = new URL(`${this.baseURL}${endpoint}`);
      if (params) {
        Object.entries(params).forEach(([key, value]) => {
          if (value !== undefined && value !== null) {
            url.searchParams.append(key, String(value));
          }
        });
      }

      const response = await fetch(url.toString(), {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      return await this.handleResponse<T>(response);
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  /**
   * POST 요청
   * 단일 책임: POST 요청만 담당
   * 에러 처리: try-catch로 예외 처리
   */
  async post<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    try {
      const response = await fetch(`${this.baseURL}${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: data ? JSON.stringify(data) : undefined,
      });

      return await this.handleResponse<T>(response);
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  /**
   * PUT 요청
   * 단일 책임: PUT 요청만 담당
   * 에러 처리: try-catch로 예외 처리
   */
  async put<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    try {
      const response = await fetch(`${this.baseURL}${endpoint}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: data ? JSON.stringify(data) : undefined,
      });

      return await this.handleResponse<T>(response);
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  /**
   * DELETE 요청
   * 단일 책임: DELETE 요청만 담당
   * 에러 처리: try-catch로 예외 처리
   */
  async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    try {
      const response = await fetch(`${this.baseURL}${endpoint}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      return await this.handleResponse<T>(response);
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  /**
   * 응답 처리
   * 단일 책임: HTTP 응답 파싱만 담당
   * 에러 처리: 상태 코드별 처리
   */
  private async handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
    // 에러 처리: HTTP 상태 코드 확인
    if (!response.ok) {
      const errorData: ErrorResponse = await response.json().catch(() => ({
        error: 'UNKNOWN_ERROR',
        message: '알 수 없는 오류가 발생했습니다.',
        status: response.status,
        timestamp: new Date().toISOString(),
      }));

      throw errorData;
    }

    const data: ApiResponse<T> = await response.json();
    return data;
  }

  /**
   * 에러 처리
   * 단일 책임: 에러 변환만 담당
   * 에러 처리: 모든 에러를 ApiResponse 형식으로 변환
   */
  private handleError<T>(error: any): ApiResponse<T> {
    console.error('API 요청 중 오류 발생:', error);

    // 에러 처리: ErrorResponse인 경우
    if (error && typeof error === 'object' && 'error' in error) {
      return {
        status: 'error',
        message: error.message || '요청 처리 중 오류가 발생했습니다.',
        data: null as T,
        timestamp: error.timestamp || new Date().toISOString(),
      };
    }

    // 에러 처리: 일반 Error인 경우
    return {
      status: 'error',
      message: error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.',
      data: null as T,
      timestamp: new Date().toISOString(),
    };
  }
}

// One Source of Truth: API 클라이언트 싱글톤 인스턴스
export const apiClient = new ApiClient();

