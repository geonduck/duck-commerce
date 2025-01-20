package kr.hhplus.be.server.domain.product.dto;

import kr.hhplus.be.server.domain.product.entity.Product;

public record ProductDomainDto(
        Long id,
        String name,
        double price,
        int stockQuantity
) {
    public static ProductDomainDto of(Product product, StockDto stock) {
        return new ProductDomainDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                stock.quantity()
        );
    }
}