package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.entity.ProductDailySales;
import kr.hhplus.be.server.domain.product.repository.ProductDailySalesRepository;
import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductDailySalesServiceTest {

    private ProductDailySalesRepository productDailySalesRepository;
    private ProductDailySalesService productDailySalesService;

    @Captor
    private ArgumentCaptor<ProductDailySales> productDailySalesCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productDailySalesRepository = mock(ProductDailySalesRepository.class);
        productDailySalesService = new ProductDailySalesService(productDailySalesRepository);
    }

    @Test
    @DisplayName("최근 3일 동안의 상위 판매 제품 조회")
    void testGetTopSellingProductsForLast3Days() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        List<ProductDailySales> mockSales = List.of(
                new ProductDailySales(1L, 101L, today, 50),
                new ProductDailySales(2L, 102L, today.minusDays(1), 40),
                new ProductDailySales(3L, 103L, today.minusDays(2), 30)
        );

        when(productDailySalesRepository.findTopSellingProductsForPeriod(
                eq(threeDaysAgo), eq(today.minusDays(1)), any())
        ).thenReturn(mockSales);

        // When
        List<ProductDailySales> topSellingProducts = productDailySalesService.getTopSellingProductsForLast3Days();

        // Then
        assertThat(topSellingProducts).hasSize(3);
        assertThat(topSellingProducts.get(0).getProductId()).isEqualTo(101L);
        assertThat(topSellingProducts.get(1).getProductId()).isEqualTo(102L);
        assertThat(topSellingProducts.get(2).getProductId()).isEqualTo(103L);

        verify(productDailySalesRepository, times(1))
                .findTopSellingProductsForPeriod(eq(threeDaysAgo), eq(today.minusDays(1)), any());
    }

    @Test
    @DisplayName("일일 판매량 업데이트")
    void testUpdateDailySales() {
        // Given
        Long productId = 101L;
        int quantitySold = 10;
        LocalDate today = LocalDate.now();

        ProductDailySales existingSales = new ProductDailySales(1L, productId, today, 20);
        when(productDailySalesRepository.findByProductIdAndDate(productId, today))
                .thenReturn(Optional.of(existingSales));

        // When
        productDailySalesService.updateDailySales(productId, quantitySold);

        // Then
        verify(productDailySalesRepository, times(1)).save(productDailySalesCaptor.capture());
        ProductDailySales savedSales = productDailySalesCaptor.getValue();
        assertThat(savedSales.getDailyQuantitySold()).isEqualTo(30);
    }

    @Test
    @DisplayName("일일 판매량 업데이트 - 새로운 기록 생성")
    void testUpdateDailySales_NewRecord() {
        // Given
        Long productId = 101L;
        int quantitySold = 10;
        LocalDate today = LocalDate.now();

        when(productDailySalesRepository.findByProductIdAndDate(productId, today))
                .thenReturn(Optional.empty());

        // When
        productDailySalesService.updateDailySales(productId, quantitySold);

        // Then
        verify(productDailySalesRepository, times(1)).save(productDailySalesCaptor.capture());
        ProductDailySales savedSales = productDailySalesCaptor.getValue();
        assertThat(savedSales.getDailyQuantitySold()).isEqualTo(10);
        assertThat(savedSales.getProductId()).isEqualTo(productId);
        assertThat(savedSales.getDate()).isEqualTo(today);
    }
}