package kr.hhplus.be.server.domain.balance;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class BalanceServiceTest {

    @Autowired
    private BalanceService balanceService;

/*    @Test
    void 최소_충전_금액_미달_시_실패() {
        BalanceRequestDto request = new BalanceRequestDto(500); // 500원 충전 시도
        assertThrows(MinimumChargeException.class, () -> balanceService.charge(request));
    }

    @Test
    void 충전_금액이_만단위가_아니면_실패() {
        BalanceRequestDto request = new BalanceRequestDto(15000); // 15,000원 충전 시도
        assertThrows(InvalidChargeAmountException.class, () -> balanceService.charge(request));
    }

    @Test
    void 최대_충전_금액_초과_시_실패() {
        BalanceRequestDto request = new BalanceRequestDto(1000000); // 1,000,000원 충전 시도
        assertThrows(MaximumChargeExceededException.class, () -> balanceService.charge(request));
    }

    @Test
    void 최대_잔액_초과_시_실패() {
        BalanceRequestDto request = new BalanceRequestDto(500000); // 현재 잔액이 500,000원일 때
        assertThrows(MaximumBalanceExceededException.class, () -> balanceService.charge(request));
    }

    @Test
    void 정상_충전_시_잔액_증가() {
        BalanceRequestDto request = new BalanceRequestDto(100000); // 정상 100,000원 충전 시도
        BalanceResponseDto response = balanceService.charge(request);
        assertEquals(100000, response.getBalance());
    }*/
}
