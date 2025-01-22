package kr.hhplus.be.server.facade.payment;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.domain.order.dto.OrderCalculationResult;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentRequestDto;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PaymentFacadeIntegrationTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BalanceService balanceService;

    @Test
    @DisplayName("결제 테스트 - 정상 흐름")
    void testCreatePayment_success() {
        // Given: 테스트 데이터 세팅
        String userId = "user1";
        Long orderId = createTestOrder(userId); // 테스트용 주문 생성
        createTestBalance(userId, 50_000); // 사용자 잔액 삽입

        // When: 결제 생성 로직 호출
        PaymentResponseDto paymentResponse = paymentFacade.createPayment(new PaymentRequestDto(userId, orderId));

        // Then: 예상 결과 검증
        assertThat(paymentResponse).isNotNull();
        assertThat(paymentResponse.status()).isEqualTo("COMPLETED");

        // 추가적으로 필요한 검증 사항: Balance 차감, Payment 기록 등의 확인
        BalanceDomainDto updatedBalance = balanceService.getBalance(new BalanceDomainDto(userId, null));
        assertThat(updatedBalance.amount()).isEqualTo(10_000);
    }

    private Long createTestOrder(String userId) {
        OrderItem item1 = OrderItem.builder()
                .productId(1L)
                .userId(userId)
                .amount(2)
                .productName("상품 A")
                .price(100.0)
                .build();

        OrderItem item2 = OrderItem.builder()
                .productId(2L)
                .userId(userId)
                .amount(1)
                .productName("상품 B")
                .price(200.0)
                .build();

        List<OrderItem> orderItems = List.of(item1, item2);
        OrderCalculationResult result = new OrderCalculationResult(40_000, orderItems);
        // 임의의 테스트 주문 생성 로직
        return orderService.createOrder(userId, result).orderId();
    }

    private void createTestBalance(String userId, double amount) {
        // 사용자 잔액 정보 삽입
        balanceService.save(Balance.builder().userId(userId).amount(amount).build());
    }


}