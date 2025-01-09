package kr.hhplus.be.server.interfaces.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "주문 관련 API", description = "주문 관련 REST API에 대한 명세를 재공 합니다")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    /**
     * TODO - 특정 유저의 주문 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "주문 조회", description = "특정 유저의 주문을 조회하는 기능",
            parameters = {@Parameter(name = "userId", description = "사용자 ID")})
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
    @Operation(summary = "주문 생성", description = "특정 유저의 주문을 생성하는 기능",
            parameters = {@Parameter(name = "OrderRequestDto", description = "사용자 ID, 주문아이템, 쿠폰 ID")})
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<OrderResponseDto>> save(@RequestBody OrderRequestDto requestDto) {

        OrderResponseDto responseDto =
                new OrderResponseDto(requestDto.userId(), 5, "쿠폰 사용");

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

}