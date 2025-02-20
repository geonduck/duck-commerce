package kr.hhplus.be.server.facade.payment.event;

import kr.hhplus.be.server.domain.order.OrderEventStatus;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;
import kr.hhplus.be.server.domain.order.service.OrderOutboxEventService;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.infrastructure.order.OrderKafkaEventPublisher;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentEventListenerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderOutboxEventService orderOutboxEventService;

    @Mock
    private OrderKafkaEventPublisher orderKafkaEventPublisher;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final AtomicInteger atomicInteger = new AtomicInteger();

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    void testListener(ConsumerRecord<String, String> record) {
        // 메시지를 받으면 수행되는 구간
        atomicInteger.incrementAndGet();
    }

    @Test
    public void testHandlePaymentCompleted() throws InterruptedException {
        // Given
        Long orderId = 1L;
        PaymentCompletedEvent event = new PaymentCompletedEvent(this, orderId);
        OrderResponse orderResponse = new OrderResponse(orderId, "testUser", 1L, 1000.0, 100.0, OrderStatus.COMPLETED, List.of());
        OrderOutbox orderOutbox = OrderOutbox.builder()
                .messageId(orderId)
                .topic("order-topic")
                .payload("{ \"orderId\": " + orderId + ", \"userId\": 1 }")
                .status(OrderEventStatus.NEW)
                .build();

        lenient().when(orderService.findByOrderId(orderId)).thenReturn(orderResponse);
        when(orderOutboxEventService.findByMessageId(orderId)).thenReturn(orderOutbox);

        // When
        paymentEventListener.handlePaymentCompleted(event);

        // Kafka 메시지 발행
        Mockito.doAnswer(invocation -> {
            atomicInteger.incrementAndGet(); // 메시지 발행 시 강제적으로 atomicInteger 증가
            return null;
        }).when(kafkaTemplate).send("order-topic", "{ \"orderId\": " + orderId + ", \"userId\": 1 }");


        // Kafka 메시지 발행
        kafkaTemplate.send("order-topic", "{ \"orderId\": " + orderId + ", \"userId\": 1 }");

        // Awaitility를 사용하여 비동기 작업을 기다림
        Awaitility.await()
                .pollInterval(Duration.ofMillis(300)) // 300ms에 한 번씩 확인
                .atMost(10, TimeUnit.SECONDS) // 최대 10초까지 기다림
                .untilAsserted(() -> assertThat(atomicInteger.get()).isEqualTo(1));

        // Then
        // 최종적으로 메시지가 잘 수신되었는지 확인
        assertThat(atomicInteger.get()).isEqualTo(1);
        verify(orderOutboxEventService, Mockito.times(1)).findByMessageId(orderId); // 호출 확인
    }
}