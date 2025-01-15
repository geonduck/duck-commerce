package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.payment.dto.PaymentDomainDto;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PaymentServiceUnitTest {

    private PaymentRepository paymentRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentService = new PaymentService(paymentRepository);
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
    @DisplayName("결제 상태 업데이트 테스트 - COMPLETED")
    void testUpdatePaymentStatus_whenCompleted() {
        // Given
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(10L)
                .paymentAmount(100.0)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        paymentService.updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);

        // Then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository).save(payment);
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