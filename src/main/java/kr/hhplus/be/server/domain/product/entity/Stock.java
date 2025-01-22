package kr.hhplus.be.server.domain.product.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseTimeEntity;
import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.product.ProductErrorCode;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Stock extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;  // 상품 ID
    private int quantity;   // 현재 재고 수량

    public void decrease(int quantity) {
        validateDecrease(quantity);
        this.quantity += quantity;
    }

    public void increase(int quantity) {
        validateIncrease(quantity);
        this.quantity += quantity;
    }

    private void validateDecrease(int quantity) {
        if (this.quantity + quantity < 0) {
            throw new DomainException(ProductErrorCode.SHORTAGE_STOCK_EXCEPTION);
        }
    }

    private void validateIncrease(int quantity) {
        if (quantity <= 0) {
            throw new DomainException(ProductErrorCode.MINIMUM_STOCK_EXCEPTION);
        }
    }
}