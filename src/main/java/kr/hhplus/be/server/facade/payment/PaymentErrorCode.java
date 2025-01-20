package kr.hhplus.be.server.facade.payment;

import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    UNABLE_PROGRESS_EXCEPTION(HttpStatus.BAD_REQUEST, "진행할 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
