package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.scheduler.ProductScheduler;
import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ProductSchedulerTest {

    private ProductDailySalesService productDailySalesService;
    private ProductScheduler productScheduler;

    @BeforeEach
    void setUp() {
        productDailySalesService = Mockito.mock(ProductDailySalesService.class);
        productScheduler = new ProductScheduler(productDailySalesService);
    }

    @Test
    @DisplayName("캐시 갱신 스케줄러 테스트")
    void testRefreshCache() {
        // When
        productScheduler.refreshCache();

        // Then
        verify(productDailySalesService, times(1)).getTopSellingProductsForLast3Days();
    }
}