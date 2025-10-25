package com.ai.CoinDoctor.controller.risk;

import com.ai.CoinDoctor.entity.RiskEventLog;
import com.ai.CoinDoctor.service.risk.RiskManagerService;
import com.ai.CoinDoctor.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 리스크 관리 컨트롤러
 * 단일 책임: 리스크 관리 관련 HTTP 요청 처리만 담당
 * One Source of Truth: 리스크 관리는 이 컨트롤러를 통해서만
 */
@Slf4j
@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트엔드 연동을 위한 CORS 설정
public class RiskController {
    
    private final RiskManagerService riskManagerService;
    
    /**
     * 현재 리스크 상태 조회
     * 단일 책임: 리스크 상태 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 현재 리스크 상태
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRiskStatus() {
        log.debug("리스크 상태 조회 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            Map<String, Object> riskStatus = riskManagerService.getCurrentRiskStatus();
            
            log.debug("리스크 상태 조회 성공");
            return ResponseEntity.ok(ApiResponse.success(riskStatus));
            
        } catch (Exception e) {
            log.error("리스크 상태 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("리스크 상태 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 연속 손실 횟수 조회
     * 단일 책임: 연속 손실 횟수 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 연속 손실 횟수
     */
    @GetMapping("/consecutive-losses")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConsecutiveLosses() {
        log.debug("연속 손실 횟수 조회 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            int consecutiveLosses = riskManagerService.getConsecutiveLossCount();
            boolean isLimitReached = riskManagerService.isConsecutiveLossLimitReached();
            
            Map<String, Object> data = new HashMap<>();
            data.put("consecutiveLosses", consecutiveLosses);
            data.put("isLimitReached", isLimitReached);
            data.put("maxAllowed", riskManagerService.getMaxConsecutiveLosses());
            
            log.debug("연속 손실 횟수 조회 성공: {} 회", consecutiveLosses);
            return ResponseEntity.ok(ApiResponse.success(data));
            
        } catch (Exception e) {
            log.error("연속 손실 횟수 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("연속 손실 횟수 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 서킷브레이커 상태 조회
     * 단일 책임: 서킷브레이커 상태 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 서킷브레이커 상태
     */
    @GetMapping("/circuit-breaker")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCircuitBreakerStatus() {
        log.debug("서킷브레이커 상태 조회 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            boolean isTriggered = riskManagerService.isCircuitBreakerTriggered();
            
            Map<String, Object> data = new HashMap<>();
            data.put("isTriggered", isTriggered);
            data.put("status", isTriggered ? "ACTIVE" : "NORMAL");
            data.put("message", isTriggered ? 
                "서킷브레이커가 발동되었습니다. 거래가 중단되었습니다." : 
                "서킷브레이커가 정상 상태입니다.");
            
            log.debug("서킷브레이커 상태 조회 성공: {}", isTriggered ? "발동" : "정상");
            return ResponseEntity.ok(ApiResponse.success(data));
            
        } catch (Exception e) {
            log.error("서킷브레이커 상태 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서킷브레이커 상태 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 쿨다운 타이머 상태 조회
     * 단일 책임: 쿨다운 타이머 상태 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 쿨다운 타이머 상태
     */
    @GetMapping("/cooldown")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCooldownStatus() {
        log.debug("쿨다운 타이머 상태 조회 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            boolean isActive = riskManagerService.isCooldownActive();
            long remainingSeconds = riskManagerService.getCooldownRemainingSeconds();
            
            Map<String, Object> data = new HashMap<>();
            data.put("isActive", isActive);
            data.put("remainingSeconds", remainingSeconds);
            data.put("remainingMinutes", remainingSeconds / 60);
            data.put("status", isActive ? "COOLING_DOWN" : "READY");
            data.put("message", isActive ? 
                String.format("쿨다운 중입니다. 남은 시간: %d초", remainingSeconds) : 
                "거래 가능 상태입니다.");
            
            log.debug("쿨다운 타이머 상태 조회 성공: {} ({}초 남음)", 
                isActive ? "활성" : "비활성", remainingSeconds);
            return ResponseEntity.ok(ApiResponse.success(data));
            
        } catch (Exception e) {
            log.error("쿨다운 타이머 상태 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("쿨다운 타이머 상태 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 거래 금액 검증
     * 단일 책임: 거래 금액 검증만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param amount 검증할 거래 금액
     * @return 검증 결과
     */
    @GetMapping("/validate-amount")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateTradeAmount(
            @RequestParam BigDecimal amount) {
        log.debug("거래 금액 검증 요청: amount={}", amount);
        
        try {
            // 에러 처리: 파라미터 검증
            if (amount == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("거래 금액이 비어있습니다."));
            }
            
            // 에러 처리: 음수 검증
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("거래 금액은 0보다 커야 합니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            boolean isValid = riskManagerService.validateTradeAmount(amount);
            BigDecimal maxAmount = riskManagerService.getMaxTradeAmount();
            
            Map<String, Object> data = new HashMap<>();
            data.put("isValid", isValid);
            data.put("amount", amount);
            data.put("maxAmount", maxAmount);
            data.put("message", isValid ? 
                "거래 금액이 유효합니다." : 
                String.format("거래 금액이 최대 한도(%s원)를 초과했습니다.", maxAmount));
            
            log.debug("거래 금액 검증 완료: amount={}, isValid={}", amount, isValid);
            return ResponseEntity.ok(ApiResponse.success(data));
            
        } catch (Exception e) {
            log.error("거래 금액 검증 중 오류 발생: amount={}", amount, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("거래 금액 검증 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 리스크 이벤트 로그 조회
     * 단일 책임: 리스크 이벤트 로그 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param startDate 시작 날짜 (yyyy-MM-dd, 선택)
     * @param endDate 종료 날짜 (yyyy-MM-dd, 선택)
     * @param eventType 이벤트 타입 (선택)
     * @return 리스크 이벤트 로그 목록
     */
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<RiskEventLog>>> getRiskEvents(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String eventType) {
        log.debug("리스크 이벤트 로그 조회 요청: startDate={}, endDate={}, eventType={}", 
            startDate, endDate, eventType);
        
        try {
            // 에러 처리: 날짜 순서 검증
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("시작 날짜는 종료 날짜보다 이전이어야 합니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            List<RiskEventLog> events = riskManagerService.getRiskEvents(startDate, endDate, eventType);
            
            log.debug("리스크 이벤트 로그 조회 성공: {} 건", events.size());
            return ResponseEntity.ok(ApiResponse.success(events));
            
        } catch (Exception e) {
            log.error("리스크 이벤트 로그 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("리스크 이벤트 로그 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 최근 리스크 이벤트 조회
     * 단일 책임: 최근 리스크 이벤트 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param limit 조회할 개수 (기본값: 10)
     * @return 최근 리스크 이벤트 목록
     */
    @GetMapping("/events/recent")
    public ResponseEntity<ApiResponse<List<RiskEventLog>>> getRecentRiskEvents(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("최근 리스크 이벤트 조회 요청: limit={}", limit);
        
        try {
            // 에러 처리: limit 범위 검증
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("조회 개수는 1~100 사이여야 합니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            List<RiskEventLog> events = riskManagerService.getRecentRiskEvents(limit);
            
            log.debug("최근 리스크 이벤트 조회 성공: {} 건", events.size());
            return ResponseEntity.ok(ApiResponse.success(events));
            
        } catch (Exception e) {
            log.error("최근 리스크 이벤트 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("최근 리스크 이벤트 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 일일 손실 한도 체크
     * 단일 책임: 일일 손실 한도 체크만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 일일 손실 한도 체크 결과
     */
    @GetMapping("/daily-loss-limit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDailyLossLimit() {
        log.debug("일일 손실 한도 체크 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            boolean isExceeded = riskManagerService.isDailyLossLimitExceeded();
            BigDecimal dailyLoss = riskManagerService.getTodayTotalLoss();
            BigDecimal maxDailyLoss = riskManagerService.getMaxDailyLoss();
            
            Map<String, Object> data = new HashMap<>();
            data.put("isExceeded", isExceeded);
            data.put("dailyLoss", dailyLoss);
            data.put("maxDailyLoss", maxDailyLoss);
            data.put("remainingAllowance", maxDailyLoss.subtract(dailyLoss));
            data.put("usagePercent", dailyLoss.divide(maxDailyLoss, 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)));
            data.put("status", isExceeded ? "LIMIT_EXCEEDED" : "NORMAL");
            data.put("message", isExceeded ? 
                "일일 손실 한도를 초과했습니다." : 
                "일일 손실 한도 내에 있습니다.");
            
            log.debug("일일 손실 한도 체크 완료: isExceeded={}, dailyLoss={}", isExceeded, dailyLoss);
            return ResponseEntity.ok(ApiResponse.success(data));
            
        } catch (Exception e) {
            log.error("일일 손실 한도 체크 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("일일 손실 한도 체크 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 리스크 설정 조회
     * 단일 책임: 리스크 설정 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 리스크 설정 정보
     */
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRiskSettings() {
        log.debug("리스크 설정 조회 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            Map<String, Object> settings = new HashMap<>();
            settings.put("maxConsecutiveLosses", riskManagerService.getMaxConsecutiveLosses());
            settings.put("maxTradeAmount", riskManagerService.getMaxTradeAmount());
            settings.put("maxDailyLoss", riskManagerService.getMaxDailyLoss());
            settings.put("cooldownDurationSeconds", riskManagerService.getCooldownDuration());
            settings.put("circuitBreakerThreshold", riskManagerService.getCircuitBreakerThreshold());
            
            log.debug("리스크 설정 조회 성공");
            return ResponseEntity.ok(ApiResponse.success(settings));
            
        } catch (Exception e) {
            log.error("리스크 설정 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("리스크 설정 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 서킷브레이커 수동 해제
     * 단일 책임: 서킷브레이커 수동 해제만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 해제 결과
     */
    @PostMapping("/circuit-breaker/reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetCircuitBreaker() {
        log.debug("서킷브레이커 수동 해제 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            riskManagerService.resetCircuitBreaker();
            
            Map<String, Object> data = new HashMap<>();
            data.put("status", "RESET");
            data.put("message", "서킷브레이커가 해제되었습니다.");
            data.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("서킷브레이커 수동 해제 완료");
            return ResponseEntity.ok(ApiResponse.success("서킷브레이커가 해제되었습니다.", data));
            
        } catch (Exception e) {
            log.error("서킷브레이커 수동 해제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서킷브레이커 해제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 쿨다운 타이머 수동 해제
     * 단일 책임: 쿨다운 타이머 수동 해제만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 해제 결과
     */
    @PostMapping("/cooldown/reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetCooldown() {
        log.debug("쿨다운 타이머 수동 해제 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            riskManagerService.resetCooldown();
            
            Map<String, Object> data = new HashMap<>();
            data.put("status", "RESET");
            data.put("message", "쿨다운 타이머가 해제되었습니다.");
            data.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("쿨다운 타이머 수동 해제 완료");
            return ResponseEntity.ok(ApiResponse.success("쿨다운 타이머가 해제되었습니다.", data));
            
        } catch (Exception e) {
            log.error("쿨다운 타이머 수동 해제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("쿨다운 타이머 해제 중 오류가 발생했습니다."));
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
        data.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success("Risk API is running", data));
    }
}
