package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BalanceErrorCode implements ErrorCode {
    NOT_FIND_EXCEPTION(HttpStatus.BAD_REQUEST, "잔액을 찾을 수 없습니다"),
    MAX_BALANCE_EXCEPTION(HttpStatus.BAD_REQUEST, "최대 10_000_000포인트까지 보유 가능합니다"),
    MINIMUM_AMOUNT_EXCEPTION(HttpStatus.UNAUTHORIZED, "최소 충전 금액은 10,000원 입니다"),
    MAXIMUM_AMOUNT_EXCEPTION(HttpStatus.PAYMENT_REQUIRED, "최대 충전 금액은 100,000원 입니다"),
    NOT_UNIT_10K_EXCEPTION(HttpStatus.FORBIDDEN, "충전 금액은 10,000원 단위여야 합니다"),
    USE_AMOUNT_EXCEPTION(HttpStatus.METHOD_NOT_ALLOWED, "사용금액은 0원 이상이여야 합니다"),
    HAVE_BALANCE_EXCEPTION(HttpStatus.NOT_ACCEPTABLE, "잔고가 부족하여 사용이 불가능 합니다"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
