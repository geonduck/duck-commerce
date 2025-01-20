package kr.hhplus.be.server.interfaces.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> findById (@PathVariable(name = "userId") @NotBlank(message = "사용자 ID는 필수입니다.") String userId) {
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
            @RequestBody @Valid OrderRequestDto requestDto
    ) {
        log.info("createOrder start");
        OrderResponseDto response = orderFacade.createOrder(requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}