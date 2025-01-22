package kr.hhplus.be.server.facade.balance;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BalanceConcurrencyTest {

    @Autowired
    private BalanceFacade balanceFacade;

    @Autowired
    private BalanceService balanceService;

    @Test
    @DisplayName("동시성 테스트: 여러 스레드에서 동시에 잔액 충전 처리")
    void testConcurrentChargeProcesses() {
        String testUser = "testUser1";
        balanceService.save(Balance.builder().userId(testUser).amount(50_000).build());

        int threadCount = 10; // 10개의 스레드 동시에 실행

        CompletableFuture<?>[] futures = new CompletableFuture<?>[threadCount];
        for (int i = 0; i < threadCount; i++) {
            futures[i] = CompletableFuture.runAsync(() -> balanceFacade.chargeBalance(new BalanceRequestDto(testUser, 10_000)));
        }

        CompletableFuture.allOf(futures).join(); // 모든 작업이 끝날 때까지 대기

        // 최종적으로 잔액이 정확히 증가했는지 검증
        BalanceDomainDto finalBalance = balanceService.getBalance(new BalanceDomainDto(testUser, null));
        assertThat(finalBalance.amount()).isEqualTo(50_000 + 10_000 * threadCount); // 50,000 + (10 * 10,000)
    }


    @Test
    @DisplayName("동시성 테스트: 잔액 충전과 사용 요청 동시 실행")
    void testConcurrentChargeAndUse() {
        String testUser = "testUser2";
        balanceService.save(Balance.builder().userId(testUser).amount(100_000).build());

        int chargeThreadCount = 5; // 충전 스레드 개수
        int useThreadCount = 5;    // 사용 스레드 개수

        CompletableFuture<?>[] futures = new CompletableFuture<?>[chargeThreadCount + useThreadCount];

        for (int i = 0; i < chargeThreadCount; i++) {
            futures[i] = CompletableFuture.runAsync(() -> balanceFacade.chargeBalance(new BalanceRequestDto(testUser, 20_000))); // 충전 20,000원
        }

        for (int i = 0; i < useThreadCount; i++) {
            futures[chargeThreadCount + i] = CompletableFuture.runAsync(() -> balanceFacade.useBalance(new BalanceRequestDto(testUser, 15_000))); // 사용 15,000원
        }

        CompletableFuture.allOf(futures).join(); // 모든 작업이 끝날 때까지 대기

        // 최종 잔액 검증
        BalanceDomainDto finalBalance = balanceService.getBalance(new BalanceDomainDto(testUser, null));
        long expectedBalance = 100_000 + (chargeThreadCount * 20_000) - (useThreadCount * 15_000);
        assertThat(finalBalance.amount()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("동시성 테스트: 여러 사용자에 대한 잔액 충전 처리")
    void testConcurrentChargeForMultipleUsers() {
        int userCount = 5; // 5명의 사용자
        int threadCount = 10; // 각 사용자당 10개의 스레드

        for (int i = 0; i < userCount; i++) {
            String userId = "user" + i;
            balanceService.save(Balance.builder().userId(userId).amount(50_000).build());
        }

        CompletableFuture<?>[] futures = new CompletableFuture<?>[userCount * threadCount];

        for (int i = 0; i < userCount; i++) {
            String userId = "user" + i;
            for (int j = 0; j < threadCount; j++) {
                futures[i * threadCount + j] = CompletableFuture.runAsync(() -> balanceFacade.chargeBalance(new BalanceRequestDto(userId, 10_000)));
            }
        }

        CompletableFuture.allOf(futures).join(); // 모든 작업이 끝날 때까지 대기

        // 각 사용자의 최종 잔액 검증
        for (int i = 0; i < userCount; i++) {
            String userId = "user" + i;
            BalanceDomainDto finalBalance = balanceService.getBalance(new BalanceDomainDto(userId, null));
            assertThat(finalBalance.amount()).isEqualTo(50_000 + 10_000 * threadCount); // 50,000 + (10 * 10,000)
        }
    }
}