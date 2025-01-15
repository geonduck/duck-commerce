package kr.hhplus.be.server.domain.payment.repository;

import kr.hhplus.be.server.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long paymentId);

    Page<Payment> findByUserId(String userId, Pageable pageable);
}
