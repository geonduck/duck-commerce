package kr.hhplus.be.server.domain.order.dto;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.order.OrderErrorCode;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;

public record OrderItemRequest(Long productId, int amount) {
    public void validate(ProductDomainDto product) {
        if (product.stockQuantity() < amount()) {
            throw new DomainException(OrderErrorCode.SHORTAGE_STOCK_EXCEPTION);
        }
    }
}