package kr.hhplus.be.server.domain.balance.dto;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.balance.BalanceErrorCode;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import org.springframework.lang.Nullable;

public record BalanceDomainDto (
        String userId,
        @Nullable Double amount
) {
    public static BalanceDomainDto from(Balance balance) {
        return new BalanceDomainDto(
                balance.getUserId(),
                balance.getAmount()
        );
    }

    public void validateCharge() {
        if (amount < 10_000) {
            throw new DomainException(BalanceErrorCode.MINIMUM_AMOUNT_EXCEPTION);
        }
        if (amount > 100_000) {
            throw new DomainException(BalanceErrorCode.MAXIMUM_AMOUNT_EXCEPTION);
        }
        if (amount % 10_000 != 0) {
            throw new DomainException(BalanceErrorCode.NOT_UNIT_10K_EXCEPTION);
        }
    }

    public void validateUse(){
        if (amount <= 0) {
            throw new DomainException(BalanceErrorCode.USE_AMOUNT_EXCEPTION);
        }
    }
}