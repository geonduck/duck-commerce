package kr.hhplus.be.server.interfaces.order.consumer;

import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.service.OrderOutboxEventService;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.infrastructure.event.OrderEventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutBoxConsumer {

    private final OrderService orderService;
    private final OrderEventSender orderEventSender;
    private final OrderOutboxEventService orderOutboxEventService;

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    @Retryable(
            value = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Transactional
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received message: " + record.value());
        try {
            String orderId = record.key();

            OrderResponse orderResponse = orderService.findByOrderId(Long.valueOf(orderId));
            orderEventSender.send(orderResponse);
            orderOutboxEventService.updateStatus(Long.valueOf(orderId));

            log.info("Kafka 주문 이벤트 처리 완료: {}", orderId);
        } catch (Exception e) {
            log.error("Kafka 이벤트 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}
