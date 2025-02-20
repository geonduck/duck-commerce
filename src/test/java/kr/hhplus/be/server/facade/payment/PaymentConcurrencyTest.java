package kr.hhplus.be.server.facade.payment;

import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentRequestDto;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@SpringBootTest
class PaymentConcurrencyTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BalanceService balanceService;

    private static final String TEST_USER_ID = "test-user";
    private Long testOrderId; // 생성된 주문 ID 저장
    private static final double ORDER_AMOUNT = 100.0; // 주문 금액
    private static final double USER_BALANCE = 200.0; // 사용자 잔액 (충분히 설정)

    private static final int THREAD_COUNT = 20;

    @BeforeEach
    void setup() {
        // 1. 테스트용 주문 생성
        Order order = Order.builder()
                .userId(TEST_USER_ID)
                .totalAmount(ORDER_AMOUNT)
                .build();
        order = orderService.save(order);
        testOrderId = order.getId();

        // 2. 테스트용 사용자 잔액 설정
        Balance balance = Balance.builder()
                .userId(TEST_USER_ID)
                .amount(USER_BALANCE) // 충분한 잔액 설정
                .build();
        balanceService.save(balance);
    }

    @Test
    @DisplayName("동시 결제 요청 처리 테스트")
    void testConcurrentPaymentRequests() throws InterruptedException {
        // Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<?>[] futures = IntStream.range(0, THREAD_COUNT)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        PaymentRequestDto request = new PaymentRequestDto(TEST_USER_ID, testOrderId); // 결제 요청
                        PaymentResponseDto response = paymentFacade.createPayment(request);
                        results.add("Success: " + response.status()); // 성공 상태 기록
                    } catch (Exception e) {
                        results.add("Failed: " + e.getMessage()); // 실패 상태 기록
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join(); // 모든 작업이 끝날 때까지 대기
        executorService.shutdown();

        // Then
        long successCount = results.stream().filter(result -> result.startsWith("Success")).count();
        long failureCount = results.stream().filter(result -> result.startsWith("Failed")).count();

        // 1. 성공 건수는 최대 테스트 데이터를 기반으로 검증합니다.
        assertThat(successCount).isEqualTo(1); // 동일 주문에 대해 하나의 결제만 가능해야 함
        // 2. 실패 건수는 나머지 요청 수와 같아야 합니다.
        assertThat(failureCount).isEqualTo(THREAD_COUNT - 1);
    }
}