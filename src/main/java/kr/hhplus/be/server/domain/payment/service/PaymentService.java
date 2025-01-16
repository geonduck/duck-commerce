package kr.hhplus.be.server.domain.payment.service;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.payment.PaymentErrorCode;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.payment.dto.PaymentDomainDto;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentDomainDto createPayment(Long orderId, double paymentAmount) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentAmount(paymentAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentDomainDto.of(savedPayment);
    }

    @Transactional
    public void updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = findPaymentById(paymentId);

        payment.setPaymentStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDomainDto getPayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        return PaymentDomainDto.of(payment);
    }

    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new DomainException(PaymentErrorCode.NOT_FIND_EXCEPTION));
    }

}