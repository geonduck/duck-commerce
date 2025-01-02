package kr.hhplus.be.server.interfaces.order.controller;

import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.order.dto.OrderRequestDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    /**
     * TODO - 특정 유저의 주문 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> findById (@PathVariable(name = "userId") String userId) {

        List<OrderResponseDto> dtoList = new ArrayList<>();
        dtoList.add(
                new OrderResponseDto(userId, 3, "결제 대기 중"));
        dtoList.add(
                new OrderResponseDto(userId, 2, "배송 완료"));

        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    /**
     * TODO - 특정 유저의 주문을 생성하는 기능을 작성해주세요.
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<OrderResponseDto>> save(@RequestBody OrderRequestDto requestDto) {

        OrderResponseDto responseDto =
                new OrderResponseDto(requestDto.userId(), 5, "쿠폰 사용 여부 확인");

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    /**
     * TODO - 특정 유저의 주문에 쿠폰 사용 여부 확인하는 기능을 작성해주세요.
     */
    @PostMapping("/{orderId}/apply-coupon")
    public ResponseEntity<ApiResponse<OrderResponseDto>> useCoupon(@PathVariable(name = "orderId") int orderId,
                                                                   @RequestBody OrderRequestDto requestDto) {

        OrderResponseDto responseDto =
                new OrderResponseDto(requestDto.userId(), orderId, "5천원 할인");

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}