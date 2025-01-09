package kr.hhplus.be.server.infra.order.repository;

import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.infra.order.jpaRepository.OrderItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {
    private final OrderItemJpaRepository jpaRepository;

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        return jpaRepository.saveAll(orderItems);
    }
}
