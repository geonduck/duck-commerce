package kr.hhplus.be.server.facade.product;


import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.entity.ProductDailySales;
import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.interfaces.product.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final ProductDailySalesService dailySalesService;

    // 상품 상세 조회
    public ProductResponseDto getProductDetail(Long productId) {
        ProductDomainDto productDomainDto = productService.getProduct(productId);

        return new ProductResponseDto(
                productDomainDto.name(),
                productDomainDto.price(),
                productDomainDto.stockQuantity()
        );
    }

    // 상품 목록 조회
    public List<ProductResponseDto> getProductList(Pageable pageable) {
        return productService.getProducts(pageable)
                .stream()
                .map(product -> new ProductResponseDto(
                        product.name(),
                        product.price(),
                        product.stockQuantity()
                ))
                .collect(Collectors.toList());
    }

    // 상위 판매 상품 5개 조회
    public List<ProductResponseDto> getTopSellingProducts() {
        List<ProductDailySales> topSellingProducts = dailySalesService.getTopSellingProductsForLast3Days();

        return topSellingProducts.stream()
                .map(sales -> {
                    ProductDomainDto product = productService.getProduct(sales.getProductId());
                    return new ProductResponseDto(
                            product.name(),
                            product.price(),
                            sales.getDailyQuantitySold()
                    );
                })
                .collect(Collectors.toList());
    }
}