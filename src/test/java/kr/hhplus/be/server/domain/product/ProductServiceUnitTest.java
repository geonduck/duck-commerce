package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.dto.ProductListDto;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
import kr.hhplus.be.server.domain.product.dto.StockDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Test
    @DisplayName("상품 상세 조회 시 재고 정보 포함")
    void getProductDetail() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("테스트상품")
                .price(1000.0)
                .build();
        StockDto stockDto = new StockDto(productId, 100);

        when(productRepository.findById(productId)).thenReturn(Optional.ofNullable(product));
        when(stockService.getStock(productId)).thenReturn(stockDto);

        // when
        ProductDomainDto result = productService.getProduct(productId);

        // then
        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.name()).isEqualTo(product.getName());
        assertThat(result.stockQuantity()).isEqualTo(stockDto.quantity());
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 예외 발생")
    void getProductNotFound() {
        // given
        Long productId = 1L;
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 목록 페이징 조회")
    void getProductsWithPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(
                Product.builder().id(1L).name("상품1").price(1000.0).build(),
                Product.builder().id(2L).name("상품2").price(2000.0).build()
        );
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        Map<Long, StockDto> stockMap = Map.of(
                1L, new StockDto(1L, 100),
                2L, new StockDto(2L, 200)
        );

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(stockService.getStocksByProductIds(anyList())).thenReturn(stockMap);

        // when
        Page<ProductListDto> result = productService.getProducts(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).stockQuantity()).isEqualTo(100);
        assertThat(result.getContent().get(1).stockQuantity()).isEqualTo(200);
    }

    @Test
    @DisplayName("상품 수정 시 재고 조정")
    void updateProductWithStock() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("테스트상품")
                .price(1000.0)
                .build();
        ProductUpdateDto updateDto = new ProductUpdateDto(
                "수정된상품",
                2000.0,
                50  // 재고 50 증가
        );

        when(productRepository.findById(productId)).thenReturn(Optional.ofNullable(product));

        // when
        productService.updateProduct(productId, updateDto.stockAdjustment());

        // then
        verify(stockService).increase(productId, 50);
    }
}
