package kr.hhplus.be.server.interfaces.balance.controller;

import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceRequestDto;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceResponseDto;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/balance")
public class BalanceController {

    private static final Logger log = LoggerFactory.getLogger(BalanceController.class);

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<BalanceResponseDto>> findById (@PathVariable(name = "userId") String userId) {

        BalanceResponseDto responseDto =
                new BalanceResponseDto("geonduck", 30_000L, LocalDateTime.of(2024, 12, 15, 15, 30));

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<BalanceResponseDto>> save(@RequestBody BalanceRequestDto requestDto) {

        BalanceResponseDto responseDto =
                new BalanceResponseDto(requestDto.userId(), 30_000L + requestDto.amount(), LocalDateTime.of(2025, 1, 2, 15, 30));

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

}
