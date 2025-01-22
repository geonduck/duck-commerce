package kr.hhplus.be.server.domain.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseTimeEntity;
import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.order.OrderErrorCode;
import kr.hhplus.be.server.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // 사용자 ID

    private Long assignmentId; // 사용된 쿠폰 ID

    private double totalAmount; // 총 가격

    private double discountAmount; // 할인 금액

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문 상태

    public void applyDiscount(double discountAmount) {
        if(discountAmount > this.getTotalAmount()) {
            throw new DomainException(OrderErrorCode.NOT_GROW_PRICE_EXCEPTION);
        }
        this.discountAmount = discountAmount;
        this.totalAmount = this.getTotalAmount() - discountAmount;
    }

    public void cancel() {
        setOrderStatus(OrderStatus.CANCELED);
    }

    public void setOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}
