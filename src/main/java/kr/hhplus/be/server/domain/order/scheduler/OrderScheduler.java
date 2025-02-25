package kr.hhplus.be.server.domain.order.scheduler;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.order.OrderEventStatus;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;
import kr.hhplus.be.server.domain.order.service.OrderOutboxEventService;
import kr.hhplus.be.server.infrastructure.order.OrderKafkaEventPublisherImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {
    private final OrderOutboxEventService orderOutboxEventService;
    private final OrderKafkaEventPublisherImpl orderKafkaEventPublisher;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void resendNewOrderEvents() {
        log.info("Resending new order events...");
        List<OrderOutbox> newOrderEvents = orderOutboxEventService.findNewOrderEvents(OrderEventStatus.NEW);
        for (OrderOutbox orderOutbox : newOrderEvents) {
            try {
                orderKafkaEventPublisher.publishOrderEvent(orderOutbox);
                log.info("Resent order event: {}", orderOutbox.getMessageId());
            } catch (Exception e) {
                log.error("Failed to resend order event: {}", orderOutbox.getMessageId(), e);
            }
        }
    }
}
