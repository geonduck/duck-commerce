package kr.hhplus.be.server.domain.order.dto;

import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;

public record OrderItemRequest(Long productId, int amount) {
    public void validate(ProductDomainDto product) {
        if (product.stockQuantity() < amount()) {
            throw new IllegalArgumentException("재고가 부족합니다: " + product.name());
        }
    }
}