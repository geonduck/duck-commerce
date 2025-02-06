package kr.hhplus.be.server.domain.payment.dto;

import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.payment.entity.Payment;

public record PaymentDomainDto(
        Long id,
        Long orderId,
        double paymentAmount,
        PaymentStatus status
) {
    public static PaymentDomainDto of(Payment payment) {
        return new PaymentDomainDto(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentAmount(),
                payment.getPaymentStatus()
        );
    }
}
