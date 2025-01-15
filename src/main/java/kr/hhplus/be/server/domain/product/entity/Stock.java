package kr.hhplus.be.server.domain.product.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseTimeEntity;
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

    @Version
    private Long version;    // 낙관적 락을 위한 버전

    public void decrease(int quantity) {
        validateDecrease(quantity);
        this.quantity -= quantity;
    }

    public void increase(int quantity) {
        validateIncrease(quantity);
        this.quantity += quantity;
    }

    private void validateDecrease(int quantity) {
        if (this.quantity - quantity < 0) {
            throw new IllegalStateException("재고가 부족합니다");
        }
    }

    private void validateIncrease(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("증가시킬 재고는 0보다 커야 합니다");
        }
    }
}