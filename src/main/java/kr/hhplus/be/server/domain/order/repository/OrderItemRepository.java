package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.order.entity.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> saveAll(List<OrderItem> orderItems);

}
