package kr.hhplus.be.server.facade.payment;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.payment.dto.PaymentDomainDto;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PaymentFacadeTest {

    private PaymentService paymentService;
    private OrderService orderService;
    private BalanceService balanceService;
    private ProductDailySalesService productDailySalesService;
    private PaymentFacade paymentFacade;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        orderService = mock(OrderService.class);
        balanceService = mock(BalanceService.class);
        productDailySalesService = mock(ProductDailySalesService.class);

        paymentFacade = new PaymentFacade(paymentService, orderService, balanceService, productDailySalesService);
    }

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

        PaymentDomainDto mockPayment = new PaymentDomainDto(1L, orderId, paymentAmount, PaymentStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());

        when(orderService.findOrderById(orderId)).thenReturn(mockOrder);
        when(paymentService.createPayment(orderId, paymentAmount)).thenReturn(mockPayment);

        // When
        PaymentResponseDto response = paymentFacade.createPayment(userId, orderId);

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

        PaymentDomainDto mockPayment = new PaymentDomainDto(paymentId, orderId, 100.0, PaymentStatus.PENDING, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(3));

        when(paymentService.getPayment(paymentId)).thenReturn(mockPayment);
        when(orderService.getOrderItems(orderId)).thenReturn(List.of(
                new OrderItemResponse(100L, "상품A", 3), // 상품 ID 100, 수량 3
                new OrderItemResponse(101L, "상품B", 5)  // 상품 ID 101, 수량 5
        ));

        // When
        paymentFacade.updatePaymentStatus(mockPayment, PaymentStatus.COMPLETED);

        // Then
        verify(paymentService).updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);
        verify(orderService).updateOrderStatus(orderId, OrderStatus.COMPLETED);
        verify(productDailySalesService, times(2)).updateDailySales(101L, 5);
    }
}