package kr.hhplus.be.server.infrastructure.event;

import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderEventDataPlatformSender implements OrderEventSender {

    @Override
    public void send(OrderResponse order) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
