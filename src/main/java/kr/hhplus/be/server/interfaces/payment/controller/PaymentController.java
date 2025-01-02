package kr.hhplus.be.server.interfaces.payment.controller;

import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    /**
     * TODO - 특정 유저의 결제를 조회하는 기능을 작성해주세요.
     */
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
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> save(@PathVariable(name = "orderId") int orderId) {

        PaymentResponseDto responseDto =
                new PaymentResponseDto("geonduck", orderId, "결제 완료");

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

}
