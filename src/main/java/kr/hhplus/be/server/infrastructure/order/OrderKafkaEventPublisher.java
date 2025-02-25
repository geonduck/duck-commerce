package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.domain.order.entity.OrderOutbox;

public interface OrderKafkaEventPublisher {
    void publishOrderEvent(OrderOutbox orderOutbox);
}
