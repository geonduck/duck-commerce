package kr.hhplus.be.server.interfaces.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.interfaces.product.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "상품 관련 API", description = "상품 관련 REST API에 대한 명세를 재공 합니다")
public class ProductController {

    /**
     * TODO - 상품을 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "상품 조회", description = "상품을 조회하는 기능")
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
    @Operation(summary = "상품 조회 (top5)", description = "상위 5개 상품을 조회하는 기능")
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
