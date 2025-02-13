package kr.hhplus.be.server.interfaces.balance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.facade.balance.BalanceFacade;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceRequestDto;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/balance")
@Tag(name = "잔액 관련 API", description = "잔액 관련 REST API에 대한 명세를 재공 합니다")
public class BalanceController {

    private final BalanceFacade balanceFacade;

    /**
     * TODO - 특정 유저의 잔액을 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "잔액 조회", description = "특정 유저의 잔액을 조회하는 기능",
            parameters = {@Parameter(name = "userId", description = "사용자 ID")})
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<BalanceResponseDto>> findById (@PathVariable(name = "userId") @NotBlank(message = "사용자 ID는 필수입니다.") String userId) {
        log.info("findById start");
        BalanceResponseDto responseDto = balanceFacade.getBalance(userId);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    /**
     * TODO - 특정 유저의 잔액을 충전하는 기능을 작성해주세요.
     */
    @Operation(summary = "잔액 충전 (charge)", description = "특정 유저의 잔액을 충전하는 기능",
            parameters = {@Parameter(name = "BalanceRequestDto", description = "사용자 ID, 충전 금액")})
    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<BalanceResponseDto>> save(@RequestBody @Valid BalanceRequestDto requestDto) {
        log.info("save start");
        BalanceResponseDto responseDto = balanceFacade.chargeBalance(requestDto);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

}
