package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceUnitTest {

    @InjectMocks
    private BalanceService balanceService;  // 테스트할 대상

    @Mock
    private BalanceRepository balanceRepository;  // Mock 의존성

    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;  // Mock 의존성

    @Test
    @DisplayName("최소 충전금액(10,000원) 미달 시 실패")
    void charge_WhenAmountBelowMinimum_ShouldThrowException() {
        // given
        BalanceDomainDto requestDto = new BalanceDomainDto("testUser", 5_000);

        // when & then
        assertThatThrownBy(() -> balanceService.charge(requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최소 충전 금액은 10,000원 입니다");
    }

    @Test
    @DisplayName("충전 금액이 10,000원 단위가 아니면 실패")
    void charge_WhenAmountNotMultipleOf10000_ShouldThrowException() {
        // given
        BalanceDomainDto requestDto = new BalanceDomainDto("testUser", 15_000);

        // when & then
        assertThatThrownBy(() -> balanceService.charge(requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("충전 금액은 10,000원 단위여야 합니다");
    }

    @Test
    @DisplayName("최대 충전 금액(100,000원) 초과 시 실패")
    void charge_WhenAmountExceedsMaxChargeLimit_ShouldThrowException() {
        // given
        BalanceDomainDto requestDto = new BalanceDomainDto("testUser", 110_000);

        // when & then
        assertThatThrownBy(() -> balanceService.charge(requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 충전 금액은 100,000원 입니다");
    }

    @Test
    @DisplayName("최대 잔액(10,000,000원) 초과 시 실패")
    void charge_WhenTotalBalanceExceedsLimit_ShouldThrowException() {
        // given
        String userId = "testUser";
        long currentBalance = 9_950_000;
        BalanceDomainDto requestDto = new BalanceDomainDto(userId, 100_000);

        when(balanceRepository.findByUserId(userId))
                .thenReturn(Balance.builder()
                        .userId(userId)
                        .amount(currentBalance)
                        .build());

        // when & then
        assertThatThrownBy(() -> balanceService.charge(requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 보유 가능 금액은 10,000,000원 입니다");
    }

    @Test
    @DisplayName("정상 충전 시 잔액 증가 및 히스토리 기록")
    void charge_WhenValidAmount_ShouldIncreaseBalanceAndRecordHistory() {
        // given
        String userId = "testUser";
        long currentBalance = 50_000;
        long chargeAmount = 20_000;
        BalanceDomainDto requestDto = new BalanceDomainDto(userId, chargeAmount);

        Balance currentBalanceEntity = Balance.builder()
                .userId(userId)
                .amount(currentBalance)
                .build();

        Balance updatedBalanceEntity = Balance.builder()
                .userId(userId)
                .amount(currentBalance + chargeAmount)
                .build();

        BalanceHistory balanceHistory = BalanceHistory.builder()
                .userId(userId)
                .amountChanged(chargeAmount)
                .status(BalanceStaut.CHARGE).build();

        when(balanceRepository.findByUserId(userId)).thenReturn(currentBalanceEntity);
        when(balanceRepository.save(any(Balance.class))).thenReturn(updatedBalanceEntity);
        when(balanceHistoryRepository.save(any(BalanceHistory.class))).thenReturn(balanceHistory);

        // when
        BalanceDomainDto result = balanceService.charge(requestDto);

        // then
        assertThat(result.amount()).isEqualTo(currentBalance + chargeAmount);
    }

    @Test
    @DisplayName("신규 사용자 충전 시 잔액 생성")
    void charge_WhenNewUser_ShouldCreateBalanceAndHistory() {
        // given
        String userId = "newUser";
        long chargeAmount = 10_000;
        BalanceDomainDto requestDto = new BalanceDomainDto(userId, chargeAmount);

        when(balanceRepository.findByUserId(userId)).thenReturn(null);
        when(balanceRepository.save(any(Balance.class)))
                .thenReturn(Balance.builder()
                        .userId(userId)
                        .amount(chargeAmount)
                        .build());

        // when
        BalanceDomainDto result = balanceService.charge(requestDto);

        // then
        assertThat(result.amount()).isEqualTo(chargeAmount);
    }

}
