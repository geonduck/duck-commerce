package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    NOT_FIND_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하지 않는 주문입니다"),
    USER_MISMATCH_EXCEPTION(HttpStatus.UNAUTHORIZED, "사용자가 일치하지 않습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}