package kr.hhplus.be.server.infra.order.repository;

import kr.hhplus.be.server.domain.order.OrderEventStatus;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;
import kr.hhplus.be.server.domain.order.repository.OrderOutboxRepository;
import kr.hhplus.be.server.infra.order.jpaRepository.OrderOutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {
    private final OrderOutboxJpaRepository jpaRepository;

    @Override
    public void save(OrderOutbox orderOutbox) {
        jpaRepository.save(orderOutbox);
    }

    @Override
    public Optional<OrderOutbox> findByMessageId(Long orderId) {
        return jpaRepository.findByMessageId(orderId);
    }

    @Override
    public List<OrderOutbox> findByStatus(OrderEventStatus status) {
        return jpaRepository.findByStatus(status);
    }
}
