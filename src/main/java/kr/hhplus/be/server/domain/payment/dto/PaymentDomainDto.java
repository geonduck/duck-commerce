package kr.hhplus.be.server.domain.payment.dto;

import kr.hhplus.be.server.domain.payment.entity.Payment;

import java.time.LocalDateTime;

public record PaymentDomainDto(
        Long id,
        Long orderId,
        double paymentAmount,
        Payment.PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentDomainDto of(Payment payment) {
        return new PaymentDomainDto(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentAmount(),
                payment.getPaymentStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
