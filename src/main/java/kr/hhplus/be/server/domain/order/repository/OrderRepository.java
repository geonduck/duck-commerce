package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findByUserId(String userId);

    Order save(Order order);
}
