package kr.hhplus.be.server.interfaces.order.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.service.OrderOutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutBoxConsumer {

    private final OrderOutboxEventService orderOutboxEventService;

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received message: " + record.value());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(record.value());
            String orderId = jsonNode.get("orderId").asText();

            orderOutboxEventService.updateStatus(Long.valueOf(orderId));

            log.info("Kafka 주문 이벤트 처리 완료: {}", orderId);
        } catch (Exception e) {
            log.error("Kafka 이벤트 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}
