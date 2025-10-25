package com.ai.CoinDoctor.exception;

import com.ai.CoinDoctor.shared.dto.ApiResponse;
import com.ai.CoinDoctor.shared.dto.ErrorResponse;
import com.ai.CoinDoctor.shared.exceptions.ApiException;
import com.ai.CoinDoctor.shared.exceptions.RiskException;
import com.ai.CoinDoctor.shared.exceptions.TradingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * 단일 책임: 모든 예외를 일관된 형식으로 처리만 담당
 * One Source of Truth: 예외 처리는 이 핸들러를 통해서만
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * TradingException 처리
     * 단일 책임: 거래 관련 예외 처리만 담당
     * 에러 처리: 일관된 에러 응답 반환
     * 
     * @param ex TradingException
     * @return 에러 응답
     */
    @ExceptionHandler(TradingException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTradingException(TradingException ex) {
        log.error("거래 예외 발생: {}", ex.getMessage(), ex);
        
        // 하드코딩 금지: ErrorResponse 객체 사용
        ErrorResponse errorResponse = new ErrorResponse(
            "TRADING_ERROR",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    /**
     * RiskException 처리
     * 단일 책임: 리스크 관련 예외 처리만 담당
     * 에러 처리: 일관된 에러 응답 반환
     * 
     * @param ex RiskException
     * @return 에러 응답
     */
    @ExceptionHandler(RiskException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleRiskException(RiskException ex) {
        log.error("리스크 예외 발생: {}", ex.getMessage(), ex);
        
        // 하드코딩 금지: ErrorResponse 객체 사용
        ErrorResponse errorResponse = new ErrorResponse(
            "RISK_ERROR",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    /**
     * ApiException 처리
     * 단일 책임: API 호출 관련 예외 처리만 담당
     * 에러 처리: 일관된 에러 응답 반환
     * 
     * @param ex ApiException
     * @return 에러 응답
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleApiException(ApiException ex) {
        log.error("API 예외 발생: statusCode={}, message={}", 
            ex.getStatusCode(), ex.getMessage(), ex);
        
        // 하드코딩 금지: ErrorResponse 객체 사용
        ErrorResponse errorResponse = new ErrorResponse(
            "API_ERROR",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        // 에러 처리: API 상태 코드에 따라 HTTP 상태 코드 결정
        HttpStatus httpStatus = determineHttpStatus(ex.getStatusCode());
        
        return ResponseEntity
            .status(httpStatus)
            .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    /**
     * Validation 예외 처리 (@Valid 검증 실패)
     * 단일 책임: 입력값 검증 예외 처리만 담당
     * 에러 처리: 필드별 에러 메시지 반환
     * 
     * @param ex MethodArgumentNotValidException
     * @return 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.warn("입력값 검증 실패: {}", ex.getMessage());
        
        // 하드코딩 금지: 필드별 에러 메시지를 Map으로 구성
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("입력값 검증에 실패했습니다.", errors));
    }
    
    /**
     * 필수 파라미터 누락 예외 처리
     * 단일 책임: 필수 파라미터 누락 예외 처리만 담당
     * 에러 처리: 누락된 파라미터 정보 반환
     * 
     * @param ex MissingServletRequestParameterException
     * @return 에러 응답
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMissingParameterException(
            MissingServletRequestParameterException ex) {
        log.warn("필수 파라미터 누락: parameterName={}, parameterType={}", 
            ex.getParameterName(), ex.getParameterType());
        
        String message = String.format("필수 파라미터가 누락되었습니다: %s (%s)", 
            ex.getParameterName(), ex.getParameterType());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "MISSING_PARAMETER",
            message,
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message, errorResponse));
    }
    
    /**
     * 파라미터 타입 불일치 예외 처리
     * 단일 책임: 파라미터 타입 불일치 예외 처리만 담당
     * 에러 처리: 타입 불일치 정보 반환
     * 
     * @param ex MethodArgumentTypeMismatchException
     * @return 에러 응답
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.warn("파라미터 타입 불일치: name={}, value={}, requiredType={}", 
            ex.getName(), ex.getValue(), ex.getRequiredType());
        
        Class<?> requiredType = ex.getRequiredType();
        String requiredTypeName = requiredType != null ? requiredType.getSimpleName() : "unknown";
        String message = String.format("파라미터 타입이 올바르지 않습니다: %s (값: %s, 필요한 타입: %s)", 
            ex.getName(), ex.getValue(), requiredTypeName);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TYPE_MISMATCH",
            message,
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message, errorResponse));
    }
    
    /**
     * JSON 파싱 예외 처리
     * 단일 책임: JSON 파싱 예외 처리만 담당
     * 에러 처리: 파싱 실패 정보 반환
     * 
     * @param ex HttpMessageNotReadableException
     * @return 에러 응답
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleJsonParseException(
            HttpMessageNotReadableException ex) {
        log.warn("JSON 파싱 실패: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "JSON_PARSE_ERROR",
            "요청 본문의 JSON 형식이 올바르지 않습니다.",
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("JSON 파싱에 실패했습니다.", errorResponse));
    }
    
    /**
     * IllegalArgumentException 처리
     * 단일 책임: 잘못된 인자 예외 처리만 담당
     * 에러 처리: 일관된 에러 응답 반환
     * 
     * @param ex IllegalArgumentException
     * @return 에러 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("잘못된 인자: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "ILLEGAL_ARGUMENT",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    /**
     * IllegalStateException 처리
     * 단일 책임: 잘못된 상태 예외 처리만 담당
     * 에러 처리: 일관된 에러 응답 반환
     * 
     * @param ex IllegalStateException
     * @return 에러 응답
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalStateException(
            IllegalStateException ex) {
        log.error("잘못된 상태: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "ILLEGAL_STATE",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    /**
     * NullPointerException 처리
     * 단일 책임: Null 포인터 예외 처리만 담당
     * 에러 처리: 일관된 에러 응답 반환
     * 
     * @param ex NullPointerException
     * @return 에러 응답
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNullPointerException(
            NullPointerException ex) {
        log.error("Null 포인터 예외 발생", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "NULL_POINTER",
            "예상치 못한 null 값이 발생했습니다.",
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("서버 내부 오류가 발생했습니다.", errorResponse));
    }
    
    /**
     * 모든 예외 처리 (최종 fallback)
     * 단일 책임: 처리되지 않은 모든 예외 처리만 담당
     * 에러 처리: 일관된 에러 응답 반환
     * 
     * @param ex Exception
     * @return 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGeneralException(Exception ex) {
        log.error("예상치 못한 예외 발생: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다.",
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("서버 내부 오류가 발생했습니다.", errorResponse));
    }
    
    /**
     * API 상태 코드를 HTTP 상태 코드로 변환
     * 단일 책임: 상태 코드 변환만 담당
     * 하드코딩 금지: 범위별로 상태 코드 결정
     * 
     * @param statusCode API 상태 코드
     * @return HTTP 상태 코드
     */
    private HttpStatus determineHttpStatus(int statusCode) {
        // 에러 처리: 상태 코드 범위별로 HTTP 상태 결정
        if (statusCode >= 200 && statusCode < 300) {
            return HttpStatus.OK;
        } else if (statusCode >= 400 && statusCode < 500) {
            return HttpStatus.BAD_REQUEST;
        } else if (statusCode >= 500) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
