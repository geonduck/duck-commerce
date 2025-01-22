package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.dto.ProductListDto;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
import kr.hhplus.be.server.domain.product.dto.StockDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.repository.StockRepository;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockService stockService;

    @Nested
    @DisplayName("상품 단건 조회")
    class GetProduct {

        @Test
        @DisplayName("상품과 재고 정보를 함께 조회한다")
        void getProductSuccess() {
            // given
            Long productId = 1L;
            Product product = createProduct(productId, "테스트상품", 1000.0);
            StockDto stockDto = new StockDto(productId, 100);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(stockService.getStock(productId)).thenReturn(stockDto);

            // when
            ProductDomainDto result = productService.getProduct(productId);

            // then
            assertThat(result.id()).isEqualTo(productId);
            assertThat(result.name()).isEqualTo("테스트상품");
            assertThat(result.price()).isEqualTo(1000.0);
            assertThat(result.stockQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회시 예외가 발생한다")
        void throwExceptionWhenProductNotFound() {
            // given
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(productId))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("상품 목록과 재고 정보를 함께 조회한다")
        void getProductsWithStock() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = List.of(
                    createProduct(1L, "상품1", 1000.0),
                    createProduct(2L, "상품2", 2000.0)
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
        @DisplayName("재고 정보가 없는 상품의 재고는 0으로 표시된다")
        void getProductsWithMissingStock() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = List.of(
                    createProduct(1L, "상품1", 1000.0),
                    createProduct(2L, "상품2", 2000.0)
            );
            Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
            Map<Long, StockDto> stockMap = Map.of(
                    1L, new StockDto(1L, 100)
                    // 2L의 재고 정보 없음
            );

            when(productRepository.findAll(pageable)).thenReturn(productPage);
            when(stockService.getStocksByProductIds(anyList())).thenReturn(stockMap);

            // when
            Page<ProductListDto> result = productService.getProducts(pageable);

            // then
            assertThat(result.getContent().get(0).stockQuantity()).isEqualTo(100);
            assertThat(result.getContent().get(1).stockQuantity()).isZero();
        }
    }

    @Nested
    @DisplayName("상품 재고 수정")
    class UpdateProduct {

        @Test
        @DisplayName("재고 증가 수정이 성공적으로 처리된다")
        void increaseStockSuccess() {
            // given
            Long productId = 1L;
            Product product = createProduct(productId, "테스트상품", 1000.0);
            int increaseAmount = 10;

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            Stock stock = createStock(productId, 100);  // 초기 재고 100
            lenient().when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
            StockDto stockDto = new StockDto(productId, 110);
            when(stockService.adjust(any(ProductUpdateDto.class))).thenReturn(stockDto);

            // when
            productService.updateProduct(new ProductUpdateDto(productId, increaseAmount));

            // then
            verify(stockService).adjust(new ProductUpdateDto(productId, increaseAmount));
            verifyNoMoreInteractions(stockService);
        }



        @Test
        @DisplayName("재고 감소 수정이 성공적으로 처리된다")
        void decreaseStockSuccess() {
            // given
            Long productId = 1L;
            Product product = createProduct(productId, "테스트상품", 1000.0);
            int decreaseAmount = -10;
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            Stock stock = createStock(productId, 100);  // 초기 재고 100
            lenient().when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

            StockDto stockDto = new StockDto(productId, 90);
            when(stockService.adjust(any(ProductUpdateDto.class))).thenReturn(stockDto);

            // when
            productService.updateProduct(new ProductUpdateDto(productId, decreaseAmount));

            // then
            verify(stockService).adjust(new ProductUpdateDto(productId, decreaseAmount));
            verifyNoMoreInteractions(stockService);
        }

        @Test
        @DisplayName("재고 수정량이 0이면 재고 서비스를 호출하지 않는다")
        void noStockUpdateWhenAmountIsZero() {
            // given
            Long productId = 1L;
            Product product = createProduct(productId, "테스트상품", 1000.0);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // when & then
            DomainException exception = assertThrows(
                    DomainException.class,
                    () -> productService.updateProduct(new ProductUpdateDto(productId, 0))
            );
            assertEquals("변경할 수량이 없습니다", exception.getMessage());
        }
    }

    private Product createProduct(Long id, String name, Double price) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(price)
                .build();
    }
    private Stock createStock(Long productId, int quantity) {
        return Stock.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}
