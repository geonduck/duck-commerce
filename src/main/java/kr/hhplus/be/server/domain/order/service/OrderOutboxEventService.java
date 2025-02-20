package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.OrderEventStatus;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;
import kr.hhplus.be.server.domain.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderOutboxEventService {
    private final OrderOutboxRepository orderOutboxRepository;

    public void saveOutboxEvent(OrderResponse orderResponse) {
        OrderOutbox orderOutbox = OrderOutbox.builder()
                .messageId(orderResponse.orderId())
                .topic("order-topic")
                .payload("{ \"orderId\": " + orderResponse.orderId()
                        + ", \"userId\": "+ orderResponse.userId()
                        + " }")
                .status(OrderEventStatus.NEW)
                .build();
        orderOutboxRepository.save(orderOutbox);
    }

    public OrderOutbox findByMessageId(Long orderId) {
        return orderOutboxRepository.findByMessageId(orderId).orElseThrow(() -> new IllegalArgumentException("주문 이벤트가 없습니다."));
    }

    public void updateStatus(Long orderId) {
        OrderOutbox outbox = this.findByMessageId(orderId);
        outbox.updateStatus(OrderEventStatus.SENT);
    }

    public void updateSendStatus(Long messageId) {
        OrderOutbox outbox = this.findByMessageId(messageId);
        outbox.updateStatus(OrderEventStatus.PROCESSING);
    }

    public List<OrderOutbox> findNewOrderEvents(OrderEventStatus status) {
        return orderOutboxRepository.findByStatus(status);
    }
}
