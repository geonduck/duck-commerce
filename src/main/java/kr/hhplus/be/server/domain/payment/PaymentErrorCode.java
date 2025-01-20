package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    NOT_FIND_EXCEPTION(HttpStatus.BAD_REQUEST, "결제를 찾을 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
