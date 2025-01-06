package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;


    @Test
    @DisplayName("재고가 존재하지 않는 상품 조회 시 예외 발생")
    void getProductWithNoStock() {
        // given
        Product product = Product.builder()
                .id(1L)
                .name("상품1")
                .price(1000)
                .stock(0)
                .build();

        when(productRepository.findById(1L)).thenReturn(product);

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            productService.getProduct(1L);
        });
    }

    @Test
    @DisplayName("실제 재고와 조회되는 재고가 다른 경우 예외 발생")
    void getProductWithMismatchedStock() {
        // given
        Product product = Product.builder()
                .id(1L)
                .name("상품1")
                .price(1000)
                .stock(10)
                .build();

        when(productRepository.findById(1L)).thenReturn(product);
        when(productRepository.getActualStock(1L)).thenReturn(5);

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            productService.verifyProductStock(1L);
        });
    }

    @Test
    @DisplayName("재고가 존재하는 정상 상품 조회 성공")
    void getProductWithStock() {
        // given
        Product product = Product.builder()
                .id(1L)
                .name("상품1")
                .price(1000)
                .stock(10)
                .build();

        when(productRepository.findById(1L)).thenReturn(product);

        // when
        ProductDomainDto result = productService.getProduct(1L);

        // then
        assertNotNull(result);
        assertEquals(10, result.stock());
        assertEquals("상품1", result.name());
        assertEquals(1000, result.price());
    }

    @Test
    @DisplayName("전체 상품 목록 페이징 조회 성공")
    void getProductsWithPagingSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        List<Product> products = IntStream.range(1, 6)
                .mapToObj(i -> Product.builder()
                        .id((long) i)
                        .name("상품" + i)
                        .price(i * 1000)
                        .stock(i * 10)
                        .build())
                .collect(Collectors.toList());

        Page<Product> productPage = new PageImpl<>(products, pageable, 15);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // when
        Page<ProductDomainDto> result = productService.getProducts(pageable);

        // then
        assertNotNull(result);
        assertEquals(5, result.getSize());
        assertEquals(0, result.getNumber());
        assertEquals(3, result.getTotalPages());
        assertEquals(15, result.getTotalElements());

        // Content 검증
        ProductDomainDto firstProduct = result.getContent().get(0);
        assertEquals(1L, firstProduct.id());
        assertEquals("상품1", firstProduct.name());
        assertEquals(1000, firstProduct.price());
        assertEquals(10, firstProduct.stock());
    }

    @Test
    @DisplayName("빈 페이지 조회 시 빈 결과 반환")
    void getProductsEmptyPageSuccess() {
        // given
        Pageable pageable = PageRequest.of(3, 5);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 15);
        when(productRepository.findAll(pageable)).thenReturn(emptyPage);

        // when
        Page<ProductDomainDto> result = productService.getProducts(pageable);

        // then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(3, result.getNumber());
        assertEquals(5, result.getSize());
    }

}
