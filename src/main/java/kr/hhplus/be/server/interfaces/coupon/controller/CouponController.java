package kr.hhplus.be.server.interfaces.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.facade.coupon.CouponFacade;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponRequestDto;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
@Tag(name = "쿠폰 관련 API", description = "쿠폰 관련 REST API에 대한 명세를 재공 합니다")
public class CouponController {

    private final CouponFacade couponFacade;

    /**
     * TODO - 특정 유저의 쿠폰 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "쿠폰 조회", description = "특정 유저의 쿠폰을 조회하는 기능",
            parameters = {@Parameter(name = "userId", description = "사용자 ID")})
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<CouponResponseDto>>> getUserCoupons(
            @PathVariable("userId") String userId,
            Pageable pageable) {
        log.info("getUserCoupons start");
        List<CouponResponseDto> coupons = couponFacade.getUserCoupons(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    /**
     * TODO - 특정 유저의 쿠폰 발급하는 기능을 작성해주세요.
     */
    @Operation(summary = "쿠폰 발급 (assign)", description = "특정 유저의 쿠폰을 발급하는 기능",
            parameters = {@Parameter(name = "CouponRequestDto", description = "사용자 ID, 쿠폰 ID")})
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<CouponResponseDto>> save(@RequestBody CouponRequestDto requestDto) {
        log.info("save start");
        CouponResponseDto responseDto = couponFacade.assignCoupon(requestDto);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    /**
     * TODO - 발급가능한 쿠폰 리스트를 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "쿠폰 리스트 조회", description = "발급 가능한 쿠폰을 확인하는 기능")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<CouponResponseDto>>> getAvailableCoupons(Pageable pageable) {
        log.info("getAvailableCoupons start");
        List<CouponResponseDto> coupons = couponFacade.getAvailableCoupons(pageable);

        return ResponseEntity.ok(ApiResponse.success(coupons));
    }
}
