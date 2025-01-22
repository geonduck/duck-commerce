package kr.hhplus.be.server.facade.payment;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.payment.dto.PaymentDomainDto;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import kr.hhplus.be.server.infrastructure.event.OrderEventSender;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentRequestDto;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentFacadeTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderService orderService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private ProductDailySalesService productDailySalesService;

    @Mock
    private OrderEventSender orderEventSender;

    @InjectMocks
    private PaymentFacade paymentFacade;

    @Test
    @DisplayName("결제 생성 테스트 - 정상 흐름")
    void testCreatePayment() {
        // Given
        String userId = "user1";
        Long orderId = 10L;
        double paymentAmount = 100.0;

        Order mockOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .totalAmount(paymentAmount)
                .build();

        OrderResponse mockOrderResponse = new OrderResponse(orderId, userId, 1L, paymentAmount, 0.0, OrderStatus.PENDING, List.of());

        PaymentDomainDto mockPayment = new PaymentDomainDto(1L, orderId, paymentAmount, PaymentStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());

        lenient().when(orderService.findOrderById(orderId)).thenReturn(mockOrder);
        when(orderService.findByOrderId(orderId)).thenReturn(mockOrderResponse);
        when(paymentService.createPayment(orderId, paymentAmount)).thenReturn(mockPayment);
        when(paymentService.updatePaymentStatus(mockPayment.id(), PaymentStatus.COMPLETED)).thenReturn(mockPayment);

        // When
        PaymentResponseDto response = paymentFacade.createPayment(new PaymentRequestDto(userId, orderId));

        // Then
        verify(balanceService).use(new BalanceDomainDto(userId, paymentAmount));
        verify(paymentService).createPayment(orderId, paymentAmount);
        verify(paymentService).updatePaymentStatus(mockPayment.id(), PaymentStatus.COMPLETED);

        assertThat(response.orderId()).isEqualTo(orderId.intValue());
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("결제 상태 업데이트 테스트")
    void testUpdatePaymentStatus() {
        // Given
        Long paymentId = 1L;
        Long orderId = 10L;
        double paymentAmount = 100.0;
        PaymentDomainDto payment = new PaymentDomainDto(paymentId, orderId, paymentAmount, PaymentStatus.PENDING, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(3));
        PaymentDomainDto mockPayment = new PaymentDomainDto(1L, orderId, paymentAmount, PaymentStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());

        lenient().when(paymentService.getPayment(paymentId)).thenReturn(payment);
        when(orderService.getOrderItems(orderId)).thenReturn(List.of(
                new OrderItemResponse(100L, "상품A", 3), // 상품 ID 100, 수량 3
                new OrderItemResponse(101L, "상품B", 5)  // 상품 ID 101, 수량 5
        ));
        when(paymentService.updatePaymentStatus(mockPayment.id(), PaymentStatus.COMPLETED)).thenReturn(mockPayment);

        // When
        paymentFacade.updatePaymentStatus(mockPayment, PaymentStatus.COMPLETED);

        // Then
        verify(paymentService).updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);
        verify(orderService).updateOrderStatus(orderId, OrderStatus.COMPLETED);
        verify(productDailySalesService, times(1)).updateDailySales(101L, 5);
    }

    @Test
    @DisplayName("결제 성공시 주문 이벤트 발송 테스트")
    void shouldSendOrderEventWhenPaymentCompleted() {
        // given
        Long paymentId = 1L;
        Long orderId = 1L;
        double paymentAmount = 100.0;
        PaymentDomainDto payment = new PaymentDomainDto(paymentId, orderId, paymentAmount, PaymentStatus.PENDING, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(3));
        OrderResponse orderResponse = new OrderResponse(orderId, "userId", 1L, 400.0, 0.0, OrderStatus.PENDING, List.of());
        PaymentDomainDto mockPayment = new PaymentDomainDto(1L, orderId, paymentAmount, PaymentStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());

        lenient().when(paymentService.getPayment(paymentId)).thenReturn(payment);
        when(orderService.findByOrderId(orderId)).thenReturn(orderResponse);
        when(paymentService.updatePaymentStatus(mockPayment.id(), PaymentStatus.COMPLETED)).thenReturn(mockPayment);

        // when
        paymentFacade.updatePaymentStatus(payment, PaymentStatus.COMPLETED);

        // then
        verify(orderEventSender, times(1)).send(orderResponse);
    }
}