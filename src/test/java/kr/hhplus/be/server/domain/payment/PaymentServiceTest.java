package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.payment.dto.PaymentDomainDto;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PaymentServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final String TEST_USER_ID = "test-user";
    private Long orderId;

    @BeforeEach
    void setup() {
        // 1. 주문 데이터 준비
        Order order = Order.builder()
                .userId(TEST_USER_ID)
                .totalAmount(150.0)
                .orderStatus(OrderStatus.PENDING)
                .build();
        order = orderService.save(order);
        this.orderId = order.getId();

        // 2. 사용자 잔액 데이터 준비
        Balance balance = Balance.builder()
                .userId(TEST_USER_ID)
                .amount(200.0) // 충분한 잔액 설정
                .build();
        balanceService.save(balance);
    }

    @Test
    @DisplayName("결제 생성 및 상태 변경 테스트")
    void testCreateAndUpdatePayment() {
        // Given
        double paymentAmount = 150.0;

        // When: 결제 생성
        PaymentDomainDto payment = paymentService.createPayment(orderId, paymentAmount);

        // Then: 결제가 정상적으로 생성되었는지 검증
        assertThat(payment).isNotNull();
        assertThat(payment.orderId()).isEqualTo(orderId);
        assertThat(payment.paymentAmount()).isEqualTo(paymentAmount);
        assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);

        // 기존 상태 검증
        Payment savedPayment = paymentRepository.findById(payment.id()).orElse(null);
        assertThat(savedPayment).isNotNull();

        // When: 결제 상태를 COMPLETED로 변경
        paymentService.updatePaymentStatus(payment.id(), PaymentStatus.COMPLETED);

        // Then: 상태가 정상적으로 업데이트되었는지 확인
        Payment updatedPayment = paymentRepository.findById(payment.id()).orElse(null);
        assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);

    }

    @Test
    @DisplayName("결제가 실패 상태로 변경")
    void testUpdatePaymentStatusToFailed() {
        // Given
        double paymentAmount = 150.0;
        PaymentDomainDto payment = paymentService.createPayment(orderId, paymentAmount);

        // When: 결제를 실패 상태로 업데이트
        paymentService.updatePaymentStatus(payment.id(), PaymentStatus.FAILED);

        // Then: 결제 실패 상태 확인
        Payment updatedPayment = paymentRepository.findById(payment.id()).orElse(null);
        assertThat(updatedPayment).isNotNull();
        assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);

    }

    @Test
    @DisplayName("결제 데이터 조회 테스트")
    void testGetPayment() {
        // Given
        double paymentAmount = 150.0;
        PaymentDomainDto payment = paymentService.createPayment(orderId, paymentAmount);

        // When: 결제 조회
        PaymentDomainDto retrievedPayment = paymentService.getPayment(payment.id());

        // Then: 데이터가 일치하는지 확인
        assertThat(retrievedPayment).isNotNull();
        assertThat(retrievedPayment.id()).isEqualTo(payment.id());
        assertThat(retrievedPayment.orderId()).isEqualTo(payment.orderId());
        assertThat(retrievedPayment.paymentAmount()).isEqualTo(payment.paymentAmount());
        assertThat(retrievedPayment.status()).isEqualTo(payment.status());
    }
}