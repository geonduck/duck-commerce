package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.payment.dto.PaymentDomainDto;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PaymentServiceUnitTest {

    private PaymentRepository paymentRepository;
    private ProductDailySalesService productDailySalesService;
    private OrderService orderService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        productDailySalesService = mock(ProductDailySalesService.class);
        orderService = mock(OrderService.class);
        paymentService = new PaymentService(paymentRepository, productDailySalesService, orderService);
    }

    @Test
    @DisplayName("결제 생성 테스트")
    void testCreatePayment() {
        // Given
        Long orderId = 10L;
        double paymentAmount = 100.0;

        Payment savedPayment = Payment.builder()
                .id(1L)
                .orderId(orderId)
                .paymentAmount(paymentAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentDomainDto result = paymentService.createPayment(orderId, paymentAmount);

        // Then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.paymentAmount()).isEqualTo(paymentAmount);
        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 상태 업데이트 및 일일 판매량 반영")
    void testUpdatePaymentStatus_whenCompleted_shouldUpdateDailySales() {
        // Given
        Long paymentId = 1L;
        Long orderId = 10L;

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .paymentAmount(100.0)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        List<OrderItemResponse> orderItems = List.of(
                new OrderItemResponse(2L, "상품A", 2), // 상품 ID 2, 수량 2
                new OrderItemResponse(5L, "상품B", 3)  // 상품 ID 5, 수량 3
        );
        when(orderService.getOrderItems(orderId)).thenReturn(orderItems);

        // When
        paymentService.updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);

        // Then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);

        verify(productDailySalesService, times(1)).updateDailySales(2L, 2);
        verify(productDailySalesService, times(1)).updateDailySales(5L, 3);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment savedPayment = captor.getValue();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("결제 조회 실패 테스트")
    void testGetPayment_whenNotFound_shouldThrowException() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(DomainException.class,
                () -> paymentService.getPayment(paymentId));
        assertThat(exception.getMessage()).isEqualTo("결제를 찾을 수 없습니다");
    }
}