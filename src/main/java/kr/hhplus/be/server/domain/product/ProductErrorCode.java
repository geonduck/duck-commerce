package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    NOT_FIND_EXCEPTION(HttpStatus.BAD_REQUEST, "상품을 찾을 수 없습니다"),
    NOT_FIND_STOCK_EXCEPTION(HttpStatus.BAD_REQUEST, "재고를 찾을 수 없습니다"),
    NON_QUANTITY_EXCEPTION(HttpStatus.UNAUTHORIZED, "변경할 수량이 없습니다"),
    SHORTAGE_STOCK_EXCEPTION(HttpStatus.UNAUTHORIZED, "재고가 부족합니다"),
    MINIMUM_STOCK_EXCEPTION(HttpStatus.UNAUTHORIZED, "증가시킬 재고는 0보다 커야 합니다"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
