package kr.hhplus.be.server.interfaces.product.controller;

import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.product.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    /**
     * TODO - 상품을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getList () {

        List<ProductResponseDto> dtoList = new ArrayList<>();
        dtoList.add(
                new ProductResponseDto("반짝반짝 자라나라 머리머리", 300_000, 50));
        dtoList.add(
                new ProductResponseDto("춤추는 째와 노래하는 희", 200_000, 20));
        dtoList.add(
                new ProductResponseDto("흩날리는 민의 눈물", 130_000, 60));

        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }

    /**
     * TODO - 상위 5개 상품을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/top5")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getTopList() {

        List<ProductResponseDto> dtoList = new ArrayList<>();
        dtoList.add(
                new ProductResponseDto("반짝반짝 자라나라 머리머리", 300_000, 15));
        dtoList.add(
                new ProductResponseDto("춤추는 째와 노래하는 희", 200_000, 10));
        dtoList.add(
                new ProductResponseDto("흩날리는 민의 눈물", 130_000, 6));

        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }
}
