package kr.hhplus.be.server.config.http;

import kr.hhplus.be.server.config.exception.CommonException;
import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonResponse {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(CommonException e) {
        log.error("ErrorCause={} \n StackTrace={}", e, e.getStackTrace()[0]);
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors()
                .get(0)
                .getDefaultMessage(); // 첫 번째 에러 메시지 가져오기

        log.warn("Validation 실패: {}", errorMessage);

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception 발생", e);

        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }

}
