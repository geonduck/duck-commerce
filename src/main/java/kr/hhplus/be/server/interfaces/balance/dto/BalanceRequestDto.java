package kr.hhplus.be.server.interfaces.balance.dto;

public record BalanceRequestDto (
        String userId,
        long amount
) {

}
