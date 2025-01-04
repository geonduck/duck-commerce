package kr.hhplus.be.server.interfaces.coupon.controller;

import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponRequestDto;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    /**
     * TODO - 특정 유저의 쿠폰 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<CouponResponseDto>>> findById (@PathVariable(name = "userId") String userId) {

        List<CouponResponseDto> dtoList = new ArrayList<>();
        dtoList.add(
                new CouponResponseDto(3, "3천원 할일", 1));
        dtoList.add(
                new CouponResponseDto(2, "2천원 할인", 1));

        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    /**
     * TODO - 특정 유저의 쿠폰 발급하는 기능을 작성해주세요.
     */
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<CouponResponseDto>> save(@RequestBody CouponRequestDto requestDto) {

        CouponResponseDto responseDto =
                new CouponResponseDto(requestDto.couponId(), "5천원 할인", 1);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    /**
     * TODO - 발급가능한 쿠폰 리스트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<CouponResponseDto>>> getList () {

        List<CouponResponseDto> dtoList = new ArrayList<>();
        dtoList.add(
                new CouponResponseDto(3, "3천원 할일", 29));
        dtoList.add(
                new CouponResponseDto(2, "2천원 할인", 15));

        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }
}