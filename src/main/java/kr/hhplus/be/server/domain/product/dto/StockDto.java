package kr.hhplus.be.server.domain.product.dto;

import kr.hhplus.be.server.domain.product.entity.Stock;

public record StockDto(
        Long productId,
        int quantity
) {
    public static StockDto from(Stock stock) {
        return new StockDto(
                stock.getProductId(),
                stock.getQuantity()
        );
    }
}