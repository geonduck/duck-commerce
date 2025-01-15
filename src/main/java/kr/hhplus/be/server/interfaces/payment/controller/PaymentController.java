package kr.hhplus.be.server.interfaces.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "결제 관련 API", description = "결제 관련 REST API에 대한 명세를 재공 합니다")
public class PaymentController {

    /**
     * TODO - 특정 유저의 결제를 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "결제 조회", description = "특정 유저의 결제을 조회하는 기능",
            parameters = {@Parameter(name = "userId", description = "사용자 ID")})
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> findById (@PathVariable(name = "userId") String userId) {

        List<PaymentResponseDto> dtoList = new ArrayList<>();
        dtoList.add(
                new PaymentResponseDto(userId, 3, "결제 대기 중"));
        dtoList.add(
                new PaymentResponseDto(userId, 2, "결제 완료"));

        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    /**
     * TODO - 특정 유저의 결제를 진행하는 기능을 작성해주세요.
     */
    @Operation(summary = "결제 진행", description = "주문에 대한 결제을 진행하는 기능",
            parameters = {@Parameter(name = "orderId", description = "주문 ID")})
    @PostMapping("/pay/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> save(@PathVariable(name = "orderId") int orderId) {

        PaymentResponseDto responseDto =
                new PaymentResponseDto("geonduck", orderId, "결제 완료");

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

}
