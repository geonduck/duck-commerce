package kr.hhplus.be.server.interfaces.balance.dto;

import java.time.LocalDateTime;

public record BalanceResponseDto (
        String userId,
        double amount,
        LocalDateTime lastUpdated
) {

}
