package kr.hhplus.be.server.domain.product.dto;

import kr.hhplus.be.server.domain.product.entity.Product;

public record ProductListDto(
        Long id,
        String name,
        double price,
        int stockQuantity
) {
    public static ProductListDto of(Product product, StockDto stock) {
        return new ProductListDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                stock != null ? stock.quantity() : 0
        );
    }
}