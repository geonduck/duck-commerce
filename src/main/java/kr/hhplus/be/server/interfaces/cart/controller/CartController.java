package kr.hhplus.be.server.interfaces.cart.controller;

import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.cart.dto.CartRequestDto;
import kr.hhplus.be.server.interfaces.cart.dto.CartResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    /**
     * TODO - 특정 유저의 장바구니를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<CartResponseDto>>> findById (@PathVariable(name = "userId") String userId) {

        List<CartResponseDto> dtoList = new ArrayList<>();
        dtoList.add(
                new CartResponseDto("반짝반짝 자라나라 머리머리", 2, 2));
        dtoList.add(
                new CartResponseDto("춤추는 째와 노래하는 희", 3, 3));

        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    /**
     * TODO - 특정 유저의 장바구니를 저장하는 기능을 작성해주세요.
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponseDto>> save(@RequestBody CartRequestDto requestDto) {

        CartResponseDto responseDto =
                new CartResponseDto("흩날리는 민의 눈물", requestDto.productId(), requestDto.amount());

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}
