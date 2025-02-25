package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.OrderEventStatus;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;

import java.util.List;
import java.util.Optional;

public interface OrderOutboxRepository {
    void save(OrderOutbox orderOutbox);

    Optional<OrderOutbox> findByMessageId(Long orderId);

    List<OrderOutbox> findByStatus(OrderEventStatus status);
}
