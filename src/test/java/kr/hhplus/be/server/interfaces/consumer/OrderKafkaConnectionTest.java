package kr.hhplus.be.server.interfaces.consumer;

import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.service.OrderOutboxEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Component
class OrderKafkaConnectionTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OrderOutboxEventService orderOutboxEventService;

    private final AtomicInteger atomicInteger = new AtomicInteger();

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    void testListener(ConsumerRecord<String, String> record) {
        // 메시지를 받으면 수행되는 구간
        atomicInteger.incrementAndGet();
    }

    @Test
    void 테스트() throws InterruptedException {
        OrderResponse orderResponse = new OrderResponse(123L, "testUser", 1L, 1000.0, 100.0, null, null);
        orderOutboxEventService.saveOutboxEvent(orderResponse);

        kafkaTemplate.send("order-topic", "{ \"orderId\": 123, \"userId\": 1 }");

        // Awaitility를 사용하여 비동기 작업을 기다림
        Awaitility.await()
                .pollInterval(Duration.ofMillis(300)) // 300ms에 한 번씩 확인
                .atMost(10, TimeUnit.SECONDS) // 최대 10초까지 기다림
                .untilAsserted(() -> assertThat(atomicInteger.get()).isEqualTo(1));

        // 최종적으로 메시지가 잘 수신되었는지 확인
        assertThat(atomicInteger.get()).isEqualTo(1);
    }
}