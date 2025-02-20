package kr.hhplus.be.server.interfaces.order.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.infrastructure.event.OrderEventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataKafkaEventConsumer {

    private final OrderService orderService;
    private final OrderEventSender orderEventSender;

    @KafkaListener(topics = "order-topic", groupId = "data-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received message: " + record.value());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(record.value());
            String orderId = jsonNode.get("orderId").asText();

            OrderResponse orderResponse = orderService.findByOrderId(Long.valueOf(orderId));
            orderEventSender.send(orderResponse);

            log.info("Kafka 주문 이벤트 처리 완료: {}", orderId);
        } catch (Exception e) {
            log.error("Kafka 이벤트 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}