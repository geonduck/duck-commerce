package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BalanceConcurrencyTest {

    @Autowired
    private BalanceService balanceService;

    private final String TEST_USER_ID = "testUser";

    @Test
    @DisplayName("동시성 테스트: 여러 스레드에서 동시에 잔액 충전 처리")
    void testConcurrentChargeProcesses() throws InterruptedException {
        // 초기 데이터 세팅
        Balance initialBalance = balanceService.save(
                Balance.builder().userId(TEST_USER_ID).amount(50_000).build()
        );

        int threadCount = 10; // 10개의 스레드 동시에 실행
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 모든 스레드에서 같은 사용자에게 10,000원씩 충전
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    balanceService.charge(new BalanceDomainDto(TEST_USER_ID, 10_000.0));
                } finally {
                    latch.countDown(); // 스레드 작업 완료할 때마다 카운트다운
                }
            });
        }

        latch.await(); // 모든 스레드가 끝날 때까지 대기

        // 최종적으로 잔액이 정확히 증가했는지 검증
        BalanceDomainDto finalBalance = balanceService.getBalance(new BalanceDomainDto(TEST_USER_ID, null));
        assertThat(finalBalance.amount()).isEqualTo(50_000 + 10_000 * threadCount); // 50,000 + (10 * 10,000)
    }


    @Test
    @DisplayName("동시성 테스트: 잔액 충전과 사용 요청 동시 실행")
    void testConcurrentChargeAndUse() throws InterruptedException {
        // 초기 데이터 세팅
        Balance initialBalance = balanceService.save(
                Balance.builder().userId(TEST_USER_ID).amount(100_000).build()
        );

        int chargeThreadCount = 5; // 충전 스레드 개수
        int useThreadCount = 5;    // 사용 스레드 개수
        ExecutorService executorService = Executors.newFixedThreadPool(chargeThreadCount + useThreadCount);
        CountDownLatch latch = new CountDownLatch(chargeThreadCount + useThreadCount);

        // 5개의 스레드가 충전
        for (int i = 0; i < chargeThreadCount; i++) {
            executorService.execute(() -> {
                try {
                    balanceService.charge(new BalanceDomainDto(TEST_USER_ID, 20_000.0)); // 충전 20,000원
                } finally {
                    latch.countDown();
                }
            });
        }

        // 5개의 스레드가 사용
        for (int i = 0; i < useThreadCount; i++) {
            executorService.execute(() -> {
                try {
                    balanceService.use(new BalanceDomainDto(TEST_USER_ID, 15_000.0)); // 사용 15,000원
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 최종 잔액 검증
        BalanceDomainDto finalBalance = balanceService.getBalance(new BalanceDomainDto(TEST_USER_ID, null));
        long expectedBalance = 100_000 + (chargeThreadCount * 20_000) - (useThreadCount * 15_000);
        assertThat(finalBalance.amount()).isEqualTo(expectedBalance);
    }
}