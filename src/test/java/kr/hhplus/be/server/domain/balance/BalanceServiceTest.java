package kr.hhplus.be.server.domain.balance;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BalanceServiceTest {

    @Autowired
    private BalanceService balanceService;

    private final String TEST_USER_ID = "testUser";

    @BeforeEach
    void setup() {
        // 초기화 메서드: 테스트마다 새로운 Balance 생성
        Balance initialDto = Balance.builder()
                .userId(TEST_USER_ID)
                .amount(50_000)
                .build();
        balanceService.save(initialDto);
    }

    @Test
    @DisplayName("잔액 충전 성공")
    void testCharge_Success() {
        // given
        BalanceDomainDto chargeDto = new BalanceDomainDto(TEST_USER_ID, 20_000.0);

        // when
        BalanceDomainDto result = balanceService.charge(chargeDto);

        // then
        assertThat(result.userId()).isEqualTo(TEST_USER_ID);
        assertThat(result.amount()).isEqualTo(70_000.0); // 기존 50_000 + 20_000
    }

    @Test
    @DisplayName("최소 충전 금액 미달 시 실패")
    void testCharge_MinimumAmount_Exception() {
        // given
        BalanceDomainDto chargeDto = new BalanceDomainDto(TEST_USER_ID, 5_000.0);

        // when & then
        assertThatThrownBy(() -> balanceService.charge(chargeDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("최소 충전 금액은 10,000원 입니다");
    }

    @Test
    @DisplayName("잔액 조회 성공")
    void testGetBalance_Success() {
        // when
        BalanceDomainDto result = balanceService.getBalance(new BalanceDomainDto(TEST_USER_ID, null));

        // then
        assertThat(result.amount()).isEqualTo(50_000.0); // 초기 충전 금액
        assertThat(result.userId()).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("잔액 사용 성공")
    void testUseBalance_Success() {
        // given
        BalanceDomainDto useDto = new BalanceDomainDto(TEST_USER_ID, 30_000.0);

        // when
        BalanceDomainDto result = balanceService.use(useDto);

        // then
        assertThat(result.amount()).isEqualTo(20_000.0); // 기존 50,000 - 30,000
    }

    @Test
    @DisplayName("잔액 부족 시 사용 실패")
    void testUseBalance_InsufficientBalance() {
        // given
        BalanceDomainDto useDto = new BalanceDomainDto(TEST_USER_ID, 100_000.0);

        // when & then
        assertThatThrownBy(() -> balanceService.use(useDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("잔고가 부족하여 사용이 불가능 합니다");
    }

    @Test
    @DisplayName("잔액 사용 취소 성공")
    void testCancelUse_Success() {
        // given
        BalanceDomainDto cancelDto = new BalanceDomainDto(TEST_USER_ID, 20_000.0);
        balanceService.use(new BalanceDomainDto(TEST_USER_ID, 20_000.0)); // 먼저 20,000 사용

        // when
        BalanceDomainDto result = balanceService.cancelUse(cancelDto);

        // then
        assertThat(result.amount()).isEqualTo(50_000.0); // 기존 50,000 -> 20,000 -> 취소 후 50,000
    }

    @Test
    @DisplayName("BalanceHistory 기록 저장 검증")
    void testBalanceHistory_Save() {
        // given
        double chargeAmount = 10_000.0;
        balanceService.charge(new BalanceDomainDto(TEST_USER_ID, chargeAmount));

        // when
        List<BalanceHistory> historyList = balanceService.findAllByUserId(TEST_USER_ID);

        // then
        assertThat(historyList).isNotEmpty();
        assertThat(historyList.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(historyList.get(0).getAmountChanged()).isEqualTo(chargeAmount);
        assertThat(historyList.get(0).getStatus()).isEqualTo(BalanceStatus.CHARGE);
    }

}