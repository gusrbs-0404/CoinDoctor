/**
 * WebSocket 클라이언트
 * 단일 책임: WebSocket 연결 및 메시지 처리만 담당
 * One Source of Truth: WebSocket 연결은 이 클라이언트를 통해서만
 */

// 에러 처리: 타입 선언
type StompClient = any;
type StompMessage = any;
type StompSubscription = any;

import { WS_BASE_URL } from '@/constants/api';

/**
 * WebSocket 클라이언트 클래스
 */
class WebSocketClient {
  private client: StompClient | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 5;
  private reconnectDelay: number = 3000;

  /**
   * WebSocket 연결
   * 단일 책임: 연결 설정만 담당
   * 에러 처리: 재연결 로직 포함
   */
  connect(onConnected?: () => void, onError?: (error: any) => void): void {
    try {
      // TODO: STOMP 라이브러리 설치 후 구현
      // 현재는 임시로 연결 성공 콜백만 호출
      console.log('WebSocket 연결 준비 (STOMP 라이브러리 설치 필요)');
      
      // 임시: 연결 성공으로 처리
      setTimeout(() => {
        this.reconnectAttempts = 0;
        onConnected?.();
      }, 100);
      
      /* STOMP 라이브러리 설치 후 사용할 코드:
      this.client = new Client({
        webSocketFactory: () => new SockJS(WS_BASE_URL),
        onConnect: () => {
          console.log('WebSocket 연결 성공');
          this.reconnectAttempts = 0;
          onConnected?.();
        },
        onDisconnect: () => {
          console.log('WebSocket 연결 끊김');
          this.handleReconnect();
        },
        onStompError: (frame: any) => {
          console.error('WebSocket 에러:', frame);
          onError?.(frame);
          this.handleReconnect();
        },
        debug: (str: string) => {
          if (process.env.NODE_ENV === 'development') {
            console.log('WebSocket Debug:', str);
          }
        },
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        reconnectDelay: this.reconnectDelay,
      });
      this.client.activate();
      */
    } catch (error) {
      console.error('WebSocket 연결 중 오류 발생:', error);
      onError?.(error);
    }
  }

  /**
   * 재연결 처리
   * 단일 책임: 재연결 로직만 담당
   * 에러 처리: 최대 재연결 횟수 제한
   */
  private handleReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
      
      setTimeout(() => {
        if (this.client && !this.client.connected) {
          this.client.activate();
        }
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.error('최대 재연결 횟수 초과');
    }
  }

  /**
   * 토픽 구독
   * 단일 책임: 토픽 구독만 담당
   * 에러 처리: 연결 상태 확인
   */
  subscribe(
    topic: string,
    callback: (message: any) => void,
    onError?: (error: any) => void
  ): void {
    try {
      // 에러 처리: 클라이언트 연결 확인
      if (!this.client) {
        console.warn('WebSocket이 연결되지 않았습니다.');
        return;
      }

      this.subscribeToTopic(topic, callback);
    } catch (error) {
      console.error(`토픽 구독 중 오류 발생: ${topic}`, error);
      onError?.(error);
    }
  }

  /**
   * 실제 토픽 구독 처리
   * 단일 책임: STOMP 구독만 담당
   */
  private subscribeToTopic(topic: string, callback: (message: any) => void): void {
    // 에러 처리: 이미 구독 중인지 확인
    if (this.subscriptions.has(topic)) {
      console.warn(`이미 구독 중인 토픽: ${topic}`);
      return;
    }

    // TODO: STOMP 라이브러리 설치 후 구현
    console.log(`토픽 구독 준비: ${topic} (STOMP 라이브러리 설치 필요)`);
    
    /* STOMP 라이브러리 설치 후 사용할 코드:
    const subscription = this.client!.subscribe(topic, (message: StompMessage) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch (error) {
        console.error('메시지 파싱 오류:', error);
      }
    });
    this.subscriptions.set(topic, subscription);
    console.log(`토픽 구독 완료: ${topic}`);
    */
  }

  /**
   * 토픽 구독 해제
   * 단일 책임: 구독 해제만 담당
   */
  unsubscribe(topic: string): void {
    try {
      const subscription = this.subscriptions.get(topic);
      if (subscription) {
        subscription.unsubscribe();
        this.subscriptions.delete(topic);
        console.log(`토픽 구독 해제: ${topic}`);
      }
    } catch (error) {
      console.error(`토픽 구독 해제 중 오류 발생: ${topic}`, error);
    }
  }

  /**
   * 메시지 전송
   * 단일 책임: 메시지 전송만 담당
   * 에러 처리: 연결 상태 확인
   */
  send(destination: string, body: any): void {
    try {
      // 에러 처리: 클라이언트 연결 확인
      if (!this.client || !this.client.connected) {
        console.error('WebSocket이 연결되지 않았습니다.');
        return;
      }

      this.client.publish({
        destination,
        body: JSON.stringify(body),
      });

      console.log(`메시지 전송: ${destination}`);
    } catch (error) {
      console.error('메시지 전송 중 오류 발생:', error);
    }
  }

  /**
   * 연결 해제
   * 단일 책임: 연결 해제만 담당
   */
  disconnect(): void {
    try {
      // 모든 구독 해제
      this.subscriptions.forEach((subscription, topic) => {
        this.unsubscribe(topic);
      });

      // 클라이언트 비활성화
      if (this.client) {
        this.client.deactivate();
        this.client = null;
      }

      console.log('WebSocket 연결 해제 완료');
    } catch (error) {
      console.error('WebSocket 연결 해제 중 오류 발생:', error);
    }
  }

  /**
   * 연결 상태 확인
   * 단일 책임: 연결 상태 확인만 담당
   */
  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}

// One Source of Truth: WebSocket 클라이언트 싱글톤 인스턴스
export const wsClient = new WebSocketClient();

