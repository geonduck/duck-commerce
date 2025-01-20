package kr.hhplus.be.server.interfaces.balance.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BalanceRequestDto (

        @NotBlank(message = "사용자 ID는 필수입니다.")
        String userId,

        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 1, message = "금액은 최소 1 이상이어야 합니다.")
        long amount
) {

}
