package kr.hhplus.be.server.facade.payment.event;

import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.infrastructure.event.OrderEventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;
    private final OrderEventSender orderEventSender;

    @Async
    @TransactionalEventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            OrderResponse orderResponse = orderService.findByOrderId(event.getOrderId());
            orderEventSender.send(orderResponse);
            log.info("Payment completed event handled for order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to handle payment completed event for order ID: {}", event.getOrderId(), e);
        }
    }
}
