package kr.hhplus.be.server.domain.balance.dto;

import kr.hhplus.be.server.domain.balance.entity.Balance;

public record BalanceDomainDto (
        String userId,
        double amount
) {
    public static BalanceDomainDto from(Balance balance) {
        return new BalanceDomainDto(
                balance.getUserId(),
                balance.getAmount()
        );
    }

    public void validateCharge() {
        if (amount < 10_000) {
            throw new IllegalStateException("최소 충전 금액은 10,000원 입니다");
        }
        if (amount > 100_000) {
            throw new IllegalStateException("최대 충전 금액은 100,000원 입니다");
        }
        if (amount % 10_000 != 0) {
            throw new IllegalStateException("충전 금액은 10,000원 단위여야 합니다");
        }
    }

    public void validateUse(){
        if (amount <= 0) {
            throw new IllegalStateException("사용금액은 0원 이상이여야 합니다");
        }
    }
}