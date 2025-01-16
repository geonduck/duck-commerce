package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {
    NOT_FIND_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하지 않는 쿠폰입니다"),
    MAX_COUPON_EXCEPTION(HttpStatus.BAD_REQUEST, "쿠폰 발급 수량이 초과되었습니다"),
    ALREADY_ISSUE_EXCEPTION(HttpStatus.UNAUTHORIZED, "이미 해당 쿠폰을 발급받았습니다"),
    ALREADY_EXPIRED_EXCEPTION(HttpStatus.PAYMENT_REQUIRED, "이미 만료된 쿠폰입니다"),
    NON_EXISTENT_ISSUE_EXCEPTION(HttpStatus.FORBIDDEN, "존재하지 않는 쿠폰 발급 내역입니다"),
    NOT_USER_COUPON_EXCEPTION(HttpStatus.METHOD_NOT_ALLOWED, "해당 사용자의 쿠폰이 아닙니다"),
    NOT_USED_COUPON_EXCEPTION(HttpStatus.NOT_ACCEPTABLE, "사용 가능한 상태가 아닙니다"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
