package com.ai.CoinDoctor.controller.trading;

import com.ai.CoinDoctor.entity.TradeLog;
import com.ai.CoinDoctor.entity.TradingConfig;
import com.ai.CoinDoctor.service.trading.TradingService;
import com.ai.CoinDoctor.shared.dto.ApiResponse;
import com.ai.CoinDoctor.shared.exceptions.TradingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 자동매매 컨트롤러
 * 단일 책임: 자동매매 관련 HTTP 요청 처리만 담당
 * One Source of Truth: 자동매매 API는 이 컨트롤러를 통해서만
 */
@Slf4j
@RestController
@RequestMapping("/api/trading")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트엔드 연동을 위한 CORS 설정
public class TradingController {
    
    private final TradingService tradingService;
    
    /**
     * 자동매매 시작
     * 단일 책임: 자동매매 시작 요청 처리만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 자동매매 시작 결과
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startTrading() {
        log.info("자동매매 시작 요청 수신");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            tradingService.startTrading();
            
            // 하드코딩 금지: 응답 데이터를 Map으로 구성
            Map<String, Object> data = new HashMap<>();
            data.put("status", "RUNNING");
            data.put("message", "자동매매가 시작되었습니다.");
            data.put("timestamp", LocalDateTime.now());
            
            log.info("자동매매 시작 성공");
            return ResponseEntity.ok(ApiResponse.success("자동매매가 시작되었습니다.", data));
            
        } catch (TradingException e) {
            log.error("자동매매 시작 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (Exception e) {
            log.error("자동매매 시작 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("자동매매 시작 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 자동매매 중지
     * 단일 책임: 자동매매 중지 요청 처리만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 자동매매 중지 결과
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stopTrading() {
        log.info("자동매매 중지 요청 수신");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            tradingService.stopTrading();
            
            // 하드코딩 금지: 응답 데이터를 Map으로 구성
            Map<String, Object> data = new HashMap<>();
            data.put("status", "STOPPED");
            data.put("message", "자동매매가 중지되었습니다.");
            data.put("timestamp", LocalDateTime.now());
            
            log.info("자동매매 중지 성공");
            return ResponseEntity.ok(ApiResponse.success("자동매매가 중지되었습니다.", data));
            
        } catch (Exception e) {
            log.error("자동매매 중지 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("자동매매 중지 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 자동매매 상태 조회
     * 단일 책임: 자동매매 상태 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 자동매매 상태
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTradingStatus() {
        log.debug("자동매매 상태 조회 요청 수신");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            boolean isEnabled = tradingService.isTradingEnabled();
            
            // 하드코딩 금지: 응답 데이터를 Map으로 구성
            Map<String, Object> data = new HashMap<>();
            data.put("isEnabled", isEnabled);
            data.put("status", isEnabled ? "RUNNING" : "STOPPED");
            data.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(data));
            
        } catch (Exception e) {
            log.error("자동매매 상태 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("자동매매 상태 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 거래 설정 조회
     * 단일 책임: 거래 설정 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 거래 설정
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<TradingConfig>> getTradingConfig() {
        log.debug("거래 설정 조회 요청 수신");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            TradingConfig config = tradingService.getTradingConfig();
            
            return ResponseEntity.ok(ApiResponse.success(config));
            
        } catch (TradingException e) {
            log.error("거래 설정 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (Exception e) {
            log.error("거래 설정 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("거래 설정 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 거래 설정 업데이트
     * 단일 책임: 거래 설정 업데이트만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param config 업데이트할 거래 설정
     * @return 업데이트된 거래 설정
     */
    @PutMapping("/config")
    public ResponseEntity<ApiResponse<TradingConfig>> updateTradingConfig(
            @RequestBody TradingConfig config) {
        log.info("거래 설정 업데이트 요청 수신: {}", config);
        
        try {
            // 에러 처리: 파라미터 검증
            if (config == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("거래 설정 데이터가 비어있습니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            TradingConfig updatedConfig = tradingService.updateTradingConfig(config);
            
            log.info("거래 설정 업데이트 성공");
            return ResponseEntity.ok(
                ApiResponse.success("거래 설정이 업데이트되었습니다.", updatedConfig));
            
        } catch (TradingException e) {
            log.error("거래 설정 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (Exception e) {
            log.error("거래 설정 업데이트 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("거래 설정 업데이트 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 최근 거래 내역 조회
     * 단일 책임: 거래 내역 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param limit 조회할 개수 (기본값: 50)
     * @return 최근 거래 내역 목록
     */
    @GetMapping("/trades/recent")
    public ResponseEntity<ApiResponse<List<TradeLog>>> getRecentTrades(
            @RequestParam(defaultValue = "50") int limit) {
        log.debug("최근 거래 내역 조회 요청: limit={}", limit);
        
        try {
            // 에러 처리: 파라미터 검증
            if (limit <= 0 || limit > 1000) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("조회 개수는 1~1000 사이여야 합니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            List<TradeLog> trades = tradingService.getRecentTrades(limit);
            
            return ResponseEntity.ok(ApiResponse.success(trades));
            
        } catch (Exception e) {
            log.error("최근 거래 내역 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("최근 거래 내역 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 총 손익 조회
     * 단일 책임: 총 손익 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param hours 조회할 시간 (기본값: 24시간)
     * @return 총 손익
     */
    @GetMapping("/profit-loss")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalProfitLoss(
            @RequestParam(defaultValue = "24") int hours) {
        log.debug("총 손익 조회 요청: hours={}", hours);
        
        try {
            // 에러 처리: 파라미터 검증
            if (hours <= 0 || hours > 720) { // 최대 30일
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("조회 시간은 1~720시간 사이여야 합니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
            java.math.BigDecimal totalProfitLoss = tradingService.getTotalProfitLoss(startTime);
            
            // 하드코딩 금지: 응답 데이터를 Map으로 구성
            Map<String, Object> data = new HashMap<>();
            data.put("totalProfitLoss", totalProfitLoss);
            data.put("hours", hours);
            data.put("startTime", startTime);
            data.put("endTime", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(data));
            
        } catch (Exception e) {
            log.error("총 손익 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("총 손익 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 헬스 체크
     * 단일 책임: API 상태 확인만 담당
     * 
     * @return API 상태
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success("Trading API is running", data));
    }
}
