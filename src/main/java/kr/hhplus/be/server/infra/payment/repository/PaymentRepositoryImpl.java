package kr.hhplus.be.server.infra.payment.repository;

import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.infra.payment.jpaRepository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    public final PaymentJpaRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return jpaRepository.findById(paymentId);
    }

    @Override
    public Page<Payment> findByUserId(String userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable);
    }
}
