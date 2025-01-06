package kr.hhplus.be.server.domain.product.dto;

import kr.hhplus.be.server.domain.product.entity.Product;

public record ProductDomainDto (
        long id,
        String name,
        double price,
        int stock
){
    public static ProductDomainDto form(Product product){
        return new ProductDomainDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }

    public void validateStock() {
        if (this.stock <= 0) {
            throw new IllegalStateException("재고가 없는 상품입니다. id=" + this.id);
        }
    }

    public void validateStockMatch(int actualStock) {
        if (this.stock != actualStock) {
            throw new IllegalStateException(
                    String.format("재고 정보가 일치하지 않습니다. id=%d, displayed=%d, actual=%d",
                            this.id, this.stock, actualStock)
            );
        }
    }
}
