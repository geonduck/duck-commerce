package kr.hhplus.be.server.infrastructure.event;

import kr.hhplus.be.server.domain.order.dto.OrderResponse;

public interface OrderEventSender {
    void send(OrderResponse order);
}
