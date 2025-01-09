package kr.hhplus.be.server.interfaces.balance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceRequestDto;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/balance")
@Tag(name = "잔액 관련 API", description = "잔액 관련 REST API에 대한 명세를 재공 합니다")
public class BalanceController {

    private static final Logger log = LoggerFactory.getLogger(BalanceController.class);

    /**
     * TODO - 특정 유저의 잔액을 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "잔액 조회", description = "특정 유저의 잔액을 조회하는 기능",
            parameters = {@Parameter(name = "userId", description = "사용자 ID")})
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<BalanceResponseDto>> findById (@PathVariable(name = "userId") String userId) {

        BalanceResponseDto responseDto =
                new BalanceResponseDto("geonduck", 30_000L, LocalDateTime.of(2024, 12, 15, 15, 30));

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    /**
     * TODO - 특정 유저의 잔액을 충전하는 기능을 작성해주세요.
     */
    @Operation(summary = "잔액 충전 (charge)", description = "특정 유저의 잔액을 충전하는 기능",
            parameters = {@Parameter(name = "BalanceRequestDto", description = "사용자 ID, 충전 금액")})
    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<BalanceResponseDto>> save(@RequestBody BalanceRequestDto requestDto) {

        BalanceResponseDto responseDto =
                new BalanceResponseDto(requestDto.userId(), 30_000L + requestDto.amount(), LocalDateTime.of(2025, 1, 2, 15, 30));

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

}
