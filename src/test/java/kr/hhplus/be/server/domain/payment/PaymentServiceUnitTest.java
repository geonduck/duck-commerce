package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.entity.Payment.PaymentStatus;
import kr.hhplus.be.server.domain.product.entity.ProductDailySales;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.product.repository.ProductDailySalesRepository;
import kr.hhplus.be.server.domain.product.ProductDailySalesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PaymentServiceUnitTest {

    private PaymentRepository paymentRepository;
    private ProductDailySalesService productDailySalesService;
    private ProductDailySalesRepository productDailySalesRepository;
    private OrderService orderService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        productDailySalesRepository = mock(ProductDailySalesRepository.class);
        productDailySalesService = mock(ProductDailySalesService.class); // 수정
        orderService = mock(OrderService.class);
        paymentService = new PaymentService(paymentRepository, productDailySalesService, orderService);
    }


    @Test
    @DisplayName("결제 완료 테스트")
    void testUpdatePaymentStatus_whenPaymentCompleted_shouldUpdateDailySales() {
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

        List<OrderItem> orderItems = List.of(
                new OrderItem(101L, orderId, "geonduck", 2L, 2),
                new OrderItem(102L, orderId, "geonduck", 5L, 3)
        );
        when(orderService.getOrderItems(orderId)).thenReturn(orderItems);

        // When
        paymentService.updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);

        // Then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);

        // Mock 객체 검증
        verify(productDailySalesService, times(1)).updateDailySales(eq(2L), eq(2));
        verify(productDailySalesService, times(1)).updateDailySales(eq(5L), eq(3));

        // PaymentRepository Mock 저장 검증
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment savedPayment = captor.getValue();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }


    @Test
    @DisplayName("결제 못찾음 테스트")
    void testUpdatePaymentStatus_whenPaymentNotFound_shouldThrowException() {
        // Given
        Long paymentId = 999L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updatePaymentStatus(paymentId, PaymentStatus.COMPLETED)
        );

        assertThat(exception.getMessage()).isEqualTo("결제를 찾을 수 없습니다. ID: 999");

        // Mock 객체에 대한 상호작용 확인
        verifyNoInteractions(productDailySalesService);
    }

}

