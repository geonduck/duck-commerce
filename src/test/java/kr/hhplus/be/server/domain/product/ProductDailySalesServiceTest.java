package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.entity.ProductDailySales;
import kr.hhplus.be.server.domain.product.repository.ProductDailySalesRepository;
import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductDailySalesServiceTest {

    private ProductDailySalesRepository productDailySalesRepository;
    private ProductDailySalesService productDailySalesService;

    @BeforeEach
    void setUp() {
        productDailySalesRepository = mock(ProductDailySalesRepository.class);
        productDailySalesService = new ProductDailySalesService(productDailySalesRepository);
    }

    @Test
    @DisplayName("최근3일")
    void testGetTopSellingProductsForLast3Days() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        List<ProductDailySales> mockSales = List.of(
                new ProductDailySales(1L, 101L, today, 50, today.atStartOfDay()),
                new ProductDailySales(2L, 102L, today.minusDays(1), 40, today.atStartOfDay()),
                new ProductDailySales(3L, 103L, today.minusDays(2), 30, today.atStartOfDay())
        );

        when(productDailySalesRepository.findTopSellingProductsForPeriod(
                eq(threeDaysAgo), eq(today), any())
        ).thenReturn(mockSales);

        // When
        List<ProductDailySales> topSellingProducts = productDailySalesService.getTopSellingProductsForLast3Days();

        // Then
        assertThat(topSellingProducts).hasSize(3);
        assertThat(topSellingProducts.get(0).getProductId()).isEqualTo(101L);
        assertThat(topSellingProducts.get(1).getProductId()).isEqualTo(102L);
        assertThat(topSellingProducts.get(2).getProductId()).isEqualTo(103L);

        verify(productDailySalesRepository, times(1))
                .findTopSellingProductsForPeriod(eq(threeDaysAgo), eq(today), any());
    }
}
