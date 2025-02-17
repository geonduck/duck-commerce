package kr.hhplus.be.server.facade.payment.event;

import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.infrastructure.event.OrderEventSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.List;
import java.util.concurrent.Future;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentEventListenerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderEventSender orderEventSender;

    @InjectMocks
    private PaymentEventListener paymentEventListener;


    @Test
    public void testHandlePaymentCompleted() {
        // Given
        Long orderId = 1L;
        PaymentCompletedEvent event = new PaymentCompletedEvent(this, orderId);
        OrderResponse orderResponse = new OrderResponse(orderId, "testUser", 1L, 1000.0, 100.0, OrderStatus.COMPLETED, List.of());

        when(orderService.findByOrderId(orderId)).thenReturn(orderResponse);

        // When
        paymentEventListener.handlePaymentCompleted(event);

        // Then
        verify(orderService, Mockito.times(1)).findByOrderId(orderId); // 호출 확인
        verify(orderEventSender, Mockito.times(1)).send(orderResponse); // 호출 확인
    }
}