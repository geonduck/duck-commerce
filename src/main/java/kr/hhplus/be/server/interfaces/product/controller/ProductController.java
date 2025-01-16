package kr.hhplus.be.server.interfaces.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import kr.hhplus.be.server.config.http.ApiResponse;
import kr.hhplus.be.server.facade.product.ProductFacade;
import kr.hhplus.be.server.interfaces.product.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "상품 관련 API", description = "상품 관련 REST API에 대한 명세를 재공 합니다")
public class ProductController {

    private final ProductFacade productFacade;

    /**
     * TODO - 상품을 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "상품 조회", description = "상품을 조회하는 기능")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductList(Pageable pageable) {
        log.info("getProductList start");
        List<ProductResponseDto> productList = productFacade.getProductList(pageable);

        return ResponseEntity.ok(ApiResponse.success(productList));
    }

    /**
     * TODO - 상품을 상세 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "상품 상세 조회", description = "상품 상세 조회하는 기능",
            parameters = {@Parameter(name = "productId", description = "상품 ID")})
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable(name = "productId") @NotBlank(message = "상품 ID는 필수입니다.") Long productId) {
        log.info("getProduct start");
        ProductResponseDto product = productFacade.getProductDetail(productId);

        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * TODO - 상위 5개 상품을 조회하는 기능을 작성해주세요.
     */
    @Operation(summary = "상품 조회 (top5)", description = "상위 5개 상품을 조회하는 기능")
    @GetMapping("/top5")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getTopSellingProducts() {
        log.info("getTopSellingProducts start");
        List<ProductResponseDto> topProducts = productFacade.getTopSellingProducts();
        return ResponseEntity.ok(ApiResponse.success(topProducts));
    }
}
