package kr.hhplus.be.server.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId; // 주문 ID

    private double paymentAmount; // 결제 금액

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // 결제 상태

    private LocalDateTime createdAt; // 생성일

    private LocalDateTime updatedAt; // 수정일

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, CANCELED
    }
}
