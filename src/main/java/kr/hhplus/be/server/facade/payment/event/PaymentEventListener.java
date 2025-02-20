package kr.hhplus.be.server.facade.payment.event;

import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;
import kr.hhplus.be.server.domain.order.service.OrderOutboxEventService;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.infrastructure.event.OrderEventSender;
import kr.hhplus.be.server.infrastructure.order.OrderKafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;
    private final OrderOutboxEventService orderOutboxEventService;
    private final OrderKafkaEventPublisher orderKafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentCreated(PaymentCompletedEvent event) {
        log.info("Payment created event handled for order ID: {}", event.getOrderId());
        try {
            OrderResponse orderResponse = orderService.findByOrderId(event.getOrderId());
            orderOutboxEventService.saveOutboxEvent(orderResponse);
        } catch (Exception e) {
            log.error("Failed to handle payment created event for order ID: {}", event.getOrderId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            OrderOutbox outboxEvent = orderOutboxEventService.findByMessageId(event.getOrderId());
            orderKafkaEventPublisher.publishOrderEvent(outboxEvent);
            log.info("Payment completed event handled for order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to handle payment completed event for order ID: {}", event.getOrderId(), e);
        }
    }
}
