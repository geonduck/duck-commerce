package kr.hhplus.be.server.interfaces.balance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record BalanceResponseDto (
        @JsonProperty("user_id") String userId,
        @JsonProperty("amount") double amount,
        @JsonProperty("last_updated") LocalDateTime lastUpdated
) {

}
