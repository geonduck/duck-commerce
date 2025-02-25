package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.domain.order.OrderEventStatus;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;
import kr.hhplus.be.server.domain.order.service.OrderOutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderKafkaEventPublisherImpl implements OrderKafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderOutboxEventService orderOutboxEventService;

    @Override
    public void publishOrderEvent(OrderOutbox orderOutbox) {
        if(orderOutbox.getStatus() != OrderEventStatus.NEW) {
            return;
        }
        kafkaTemplate.send(orderOutbox.getTopic(), String.valueOf(orderOutbox.getMessageId()), orderOutbox.getPayload());
        orderOutboxEventService.updateSendStatus(orderOutbox.getMessageId());
    }
}
