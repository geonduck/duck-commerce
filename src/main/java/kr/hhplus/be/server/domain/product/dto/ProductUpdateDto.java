package kr.hhplus.be.server.domain.product.dto;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.product.ProductErrorCode;

public record ProductUpdateDto(
        Long productId, int amount
) {
    public void validateUpdate() {
        if (amount() == 0) {
            throw new DomainException(ProductErrorCode.NON_QUANTITY_EXCEPTION);
        }
    }
}