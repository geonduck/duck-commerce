package kr.hhplus.be.server.config.http;

import kr.hhplus.be.server.config.exception.CommonException;
import kr.hhplus.be.server.config.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonResponse {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(CommonException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(IllegalStateException e) {
        return ResponseEntity.internalServerError().body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }

}
