package kr.hhplus.be.server.infra.payment.jpaRepository;

import kr.hhplus.be.server.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByUserId(String userId, Pageable pageable);
}
