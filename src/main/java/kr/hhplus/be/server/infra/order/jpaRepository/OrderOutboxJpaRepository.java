package kr.hhplus.be.server.infra.order.jpaRepository;

import kr.hhplus.be.server.domain.order.OrderEventStatus;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutbox, Long> {
    Optional<OrderOutbox> findByMessageId(Long orderId);

    List<OrderOutbox> findByStatus(OrderEventStatus status);
}
