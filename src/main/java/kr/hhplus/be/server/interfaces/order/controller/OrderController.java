package kr.hhplus.be.server.interfaces.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.facade.order.OrderFacade;
import kr.hhplus.be.server.interfaces.order.dto.OrderRequestDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "주문 관련 API", description = "주문 관련 REST API에 대한 명세를 재공 합니다")
public class OrderController {

    private final OrderFacade orderFacade;

    /**
     * TODO - 특정 유저의 주문 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "주문 조회", description = "특정 유저의 주문을 조회하는 기능",
            parameters = {@Parameter(name = "userId", description = "사용자 ID")})
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> findById (@PathVariable(name = "userId") String userId) {
        log.info("findById start");
        List<OrderResponseDto> orders = orderFacade.getOrdersByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * TODO - 특정 유저의 주문을 생성하는 기능을 작성해주세요.
     */
    @Operation(summary = "주문 생성", description = "특정 유저의 주문을 생성하는 기능",
            parameters = {@Parameter(name = "OrderRequestDto", description = "주문 데이터")})
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @RequestBody OrderRequestDto requestDto
    ) {
        // 주문 생성 로직
        OrderResponseDto response = orderFacade.createOrder(requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문에 할인 적용", description = "주문에 쿠폰을 적용해 할인을 추가합니다.",
            parameters = {
                    @Parameter(name = "orderId", description = "주문 ID"),
                    @Parameter(name = "userId", description = "사용자 ID"),
                    @Parameter(name = "couponId", description = "적용할 쿠폰 ID")
            })
    @PostMapping("/discount/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> applyDiscount(
            @PathVariable Long orderId,
            @RequestParam String userId,
            @RequestParam Long couponId
    ) {
        // 주문 할인 적용 로직
        OrderResponseDto response = orderFacade.applyDiscount(userId, orderId, couponId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}