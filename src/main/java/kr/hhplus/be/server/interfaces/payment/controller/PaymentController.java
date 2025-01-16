package kr.hhplus.be.server.interfaces.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.facade.payment.PaymentFacade;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentRequestDto;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "결제 관련 API", description = "결제 관련 REST API에 대한 명세를 재공 합니다")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    /**
     * TODO - 특정 유저의 결제를 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "결제 조회", description = "특정 유저의 결제를 조회합니다.",
            parameters = {@Parameter(name = "paymentId", description = "결제 ID")})
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPayment(
            @PathVariable(name = "paymentId") @NotNull(message = "주문 ID는 필수입니다.") Long paymentId
    ) {
        log.info("getPayment start");
        PaymentResponseDto response = paymentFacade.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * TODO - 특정 유저의 결제를 진행하는 기능을 작성해주세요.
     */
    @Operation(summary = "결제 진행", description = "주문에 대한 결제를 진행합니다.",
            parameters = {
                    @Parameter(name = "PaymentRequestDto", description = "결제 데이터")
            })
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> createPayment(
            @RequestBody @Valid PaymentRequestDto requestDto
    ) {
        log.info("createPayment start");
        PaymentResponseDto response = paymentFacade.createPayment(requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
