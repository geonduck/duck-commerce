package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.dto.ProductListDto;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockService stockService;

    private Long testProductId;
    private int initialStockQuantity;

    @BeforeEach
    void setup() {
        // Given: 상품과 재고 초기 데이터 생성
        Product product = Product.builder()
                .name("테스트 상품")
                .price(1000.0)
                .build();
        Product savedProduct = productRepository.save(product);

        this.testProductId = savedProduct.getId();
        this.initialStockQuantity = 100;

        Stock stock = Stock.builder()
                .productId(testProductId)
                .quantity(initialStockQuantity)
                .build();
        stockService.save(stock);
    }

    @Test
    @DisplayName("상품 단건 조회 - 성공")
    void getProduct_success() {
        // When
        ProductDomainDto result = productService.getProduct(testProductId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testProductId);
        assertThat(result.name()).isEqualTo("테스트 상품");
        assertThat(result.price()).isEqualTo(1000.0);
        assertThat(result.stockQuantity()).isEqualTo(initialStockQuantity);
    }

    @Test
    @DisplayName("상품 단건 조회 - 실패 (없는 상품)")
    void getProduct_notFound() {
        // When & Then
        assertThatThrownBy(() -> productService.getProduct(999L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 목록 조회 - 페이지 단위")
    void getProducts_paged() {
        // When
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ProductListDto> productList = productService.getProducts(pageable);

        // Then
        assertThat(productList).isNotEmpty();
        assertThat(productList.getContent()).hasSize(1); // 현재 데이터는 1개
        ProductListDto retrievedProduct = productList.getContent().get(0);
        assertThat(retrievedProduct.id()).isEqualTo(testProductId);
        assertThat(retrievedProduct.stockQuantity()).isEqualTo(initialStockQuantity);
    }

    @Test
    @DisplayName("상품 업데이트 - 재고 수량 증가")
    void updateProduct_increaseStock() {
        // Given
        int incrementAmount = 50;
        ProductUpdateDto updateDto = new ProductUpdateDto(testProductId, incrementAmount);

        // When
        productService.updateProduct(updateDto);

        // Then
        ProductDomainDto result = productService.getProduct(testProductId);
        assertThat(result.stockQuantity()).isEqualTo(initialStockQuantity + incrementAmount);
    }

    @Test
    @DisplayName("상품 업데이트 - 재고 수량 감소")
    void updateProduct_decreaseStock() {
        // Given
        int decrementAmount = -30; // 감소량
        ProductUpdateDto updateDto = new ProductUpdateDto(testProductId, decrementAmount);

        // When
        productService.updateProduct(updateDto);

        // Then
        ProductDomainDto result = productService.getProduct(testProductId);
        assertThat(result.stockQuantity()).isEqualTo(initialStockQuantity + decrementAmount);
    }

    @Test
    @DisplayName("상품 업데이트 - 재고 부족 예외")
    void updateProduct_stockInsufficient() {
        // Given
        int decrementAmount = -(initialStockQuantity + 10); // 재고보다 많은 감소량
        ProductUpdateDto updateDto = new ProductUpdateDto(testProductId, decrementAmount);

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(updateDto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("재고가 부족합니다");
    }
}