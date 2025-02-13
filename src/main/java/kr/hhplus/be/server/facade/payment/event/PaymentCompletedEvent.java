package kr.hhplus.be.server.facade.payment.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentCompletedEvent extends ApplicationEvent {
    private final Long orderId;

    public PaymentCompletedEvent(Object source, Long orderId) {
        super(source);
        this.orderId = orderId;
    }

}