package kr.hhplus.be.server.domain.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId; // 주문 ID

    @Column(nullable = false)
    private String userId; // 사용자 ID

    @Column(nullable = false)
    private Long productId; // 상품 ID

    @Column(nullable = false)
    private int amount; // 주문 수량

    @Column(nullable = false)
    private String productName; // 상품 이름

    @Column(nullable = false)
    private double price; // 구매 당시 상품 가격 (단가)
}
