package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        BalanceDomainDto requestDto = new BalanceDomainDto("testUser", (double) 5_000);

        // when & then
        assertThatThrownBy(() -> balanceService.charge(requestDto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(BalanceErrorCode.MINIMUM_AMOUNT_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("충전 금액이 10,000원 단위가 아니면 실패")
    void charge_WhenAmountNotMultipleOf10000_ShouldThrowException() {
        // given
        BalanceDomainDto requestDto = new BalanceDomainDto("testUser", (double) 15_000);

        // when & then
        assertThatThrownBy(() -> balanceService.charge(requestDto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(BalanceErrorCode.NOT_UNIT_10K_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("최대 충전 금액(100,000원) 초과 시 실패")
    void charge_WhenAmountExceedsMaxChargeLimit_ShouldThrowException() {
        // given
        BalanceDomainDto requestDto = new BalanceDomainDto("testUser", (double) 110_000);

        // when & then
        assertThatThrownBy(() -> balanceService.charge(requestDto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(BalanceErrorCode.MAXIMUM_AMOUNT_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("최대 잔액(10,000,000원) 초과 시 실패")
    void charge_WhenTotalBalanceExceedsLimit_ShouldThrowException() {
        // given
        String userId = "testUser";
        long currentBalance = 9_950_000;
        BalanceDomainDto requestDto = new BalanceDomainDto(userId, (double) 100_000);

        when(balanceRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.ofNullable(Balance.builder()
                        .userId(userId)
                        .amount(currentBalance)
                        .build()));

        // when & then
        assertThatThrownBy(() -> balanceService.charge(requestDto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(BalanceErrorCode.MAX_BALANCE_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("정상 충전 시 잔액 증가 및 히스토리 기록")
    void charge_WhenValidAmount_ShouldIncreaseBalanceAndRecordHistory() {
        // given
        String userId = "testUser";
        long currentBalance = 50_000;
        double chargeAmount = 20_000;
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
                .status(BalanceStatus.CHARGE).build();

        when(balanceRepository.findByUserIdWithLock(userId)).thenReturn(Optional.ofNullable(currentBalanceEntity));
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
        Balance initialBalance = Balance.builder()
                .userId(userId)
                .amount(0)
                .build();
        double chargeAmount = 10_000;
        BalanceDomainDto requestDto = new BalanceDomainDto(userId, chargeAmount);

        when(balanceRepository.findByUserIdWithLock(userId)).thenReturn(Optional.ofNullable(initialBalance));
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

    @Test
    @DisplayName("정상 조회 시 잔고 반환")
    void getPoints_WithValidUser_ShouldReturnBalance() {
        // given
        String userId = "testUser";
        long expectedBalance = 50_000;
        Balance balance = Balance.builder()
                .userId(userId)
                .amount(expectedBalance)
                .build();

        when(balanceRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.ofNullable(balance));

        // when
        BalanceDomainDto result = balanceService.getBalance(new BalanceDomainDto(userId, null));

        // then
        assertThat(result.amount()).isEqualTo(expectedBalance);
        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("신규 유저는 초기 포인트 0")
    void getPoints_NewUser_ShouldReturnZero() {
        // given
        String userId = "newUser";
        Balance initialBalance = Balance.builder()
                .userId(userId)
                .amount(0)
                .build();

        when(balanceRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.ofNullable(initialBalance));

        // when
        BalanceDomainDto result = balanceService.getBalance(new BalanceDomainDto(userId, null));

        // then
        assertThat(result.amount()).isEqualTo(0);
        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("잘못된 사용자 ID로 조회 시 빈 잔고 반환")
    void getPoints_WithInvalidUser_ShouldReturnEmptyBalance() {
        // given
        String invalidUserId = "";
        Balance emptyBalance = Balance.builder()
                .userId(invalidUserId)
                .amount(0)
                .build();

        when(balanceRepository.findByUserIdWithLock(invalidUserId))
                .thenReturn(Optional.ofNullable(emptyBalance));

        // when
        BalanceDomainDto result = balanceService.getBalance(new BalanceDomainDto(invalidUserId, null));

        // then
        assertThat(result.amount()).isEqualTo(0);
        assertThat(result.userId()).isEqualTo(invalidUserId);
    }

    @Test
    @DisplayName("잔액 변경 히스토리가 정상적으로 기록되는지 확인")
    void charge_ShouldRecordBalanceHistory() {
        // given
        String userId = "testUser";
        double chargeAmount = 20_000;
        BalanceDomainDto requestDto = new BalanceDomainDto(userId, chargeAmount);

        Balance currentBalance = Balance.builder()
                .userId(userId)
                .amount(50_000)
                .build();

        when(balanceRepository.findByUserIdWithLock(userId)).thenReturn(Optional.ofNullable(currentBalance));
        when(balanceRepository.save(any(Balance.class))).thenReturn(currentBalance);

        // when
        balanceService.charge(requestDto);

        // then
        verify(balanceHistoryRepository, times(1)).save(argThat(history ->
                history.getUserId().equals(userId) &&
                        history.getAmountChanged() == chargeAmount &&
                        history.getStatus() == BalanceStatus.CHARGE
        ));
    }

    @Test
    @DisplayName("잔액이 충분할 때 포인트 사용 성공")
    void usePoint_WhenBalanceIsSufficient_ShouldSuccess() {
        // given
        String userId = "testUser";
        double currentBalance = 50_000;
        double useAmount = 30_000;

        Balance balance = Balance.builder()
                .userId(userId)
                .amount(currentBalance)
                .build();

        when(balanceRepository.findByUserIdWithLock(userId)).thenReturn(Optional.ofNullable(balance));
        when(balanceRepository.save(any(Balance.class)))
                .thenReturn(Balance.builder()
                        .userId(userId)
                        .amount(currentBalance - useAmount)
                        .build());

        // when
        BalanceDomainDto result = balanceService.use(new BalanceDomainDto(userId, useAmount));

        // then
        assertThat(result.amount()).isEqualTo(currentBalance - useAmount);
    }

    @Test
    @DisplayName("잔액이 부족할 때 포인트 사용 실패")
    void usePoint_WhenInsufficientBalance_ShouldThrowException() {
        // given
        String userId = "testUser";
        double currentBalance = 10_000;
        double useAmount = 20_000;

        Balance balance = Balance.builder()
                .userId(userId)
                .amount(currentBalance)
                .build();

        when(balanceRepository.findByUserIdWithLock(userId)).thenReturn(Optional.ofNullable(balance));

        // when & then
        assertThatThrownBy(() -> balanceService.use(new BalanceDomainDto(userId, useAmount)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(BalanceErrorCode.HAVE_BALANCE_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("사용 금액이 0 이하일 때 실패")
    void usePoint_WhenAmountIsZeroOrNegative_ShouldThrowException() {
        // given
        String userId = "testUser";
        double currentBalance = 10_000;
        double useAmount = -1;

        Balance balance = Balance.builder()
                .userId(userId)
                .amount(currentBalance)
                .build();

        lenient().when(balanceRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.ofNullable(balance));

        // when & then
        assertThatThrownBy(() -> balanceService.use(new BalanceDomainDto(userId, useAmount)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(BalanceErrorCode.USE_AMOUNT_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("사용 취소 시 잔액 복구")
    void cancelUsePoint_ShouldRestoreBalance() {
        // given
        String userId = "testUser";
        double currentBalance = 20_000;
        double cancelAmount = 10_000;

        Balance balance = Balance.builder()
                .userId(userId)
                .amount(currentBalance)
                .build();

        when(balanceRepository.findByUserIdWithLock(userId)).thenReturn(Optional.ofNullable(balance));
        when(balanceRepository.save(any(Balance.class)))
                .thenReturn(Balance.builder()
                        .userId(userId)
                        .amount(currentBalance + cancelAmount)
                        .build());

        // when
        BalanceDomainDto result = balanceService.cancelUse(new BalanceDomainDto(userId, cancelAmount));

        // then
        assertThat(result.amount()).isEqualTo(currentBalance + cancelAmount);
        verify(balanceHistoryRepository).save(argThat(history ->
                history.getUserId().equals(userId) &&
                        history.getAmountChanged() == cancelAmount &&
                        history.getStatus() == BalanceStatus.CANCEL
        ));
    }

}
