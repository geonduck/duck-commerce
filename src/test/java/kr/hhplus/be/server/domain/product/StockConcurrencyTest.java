package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
import kr.hhplus.be.server.domain.product.dto.StockDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StockConcurrencyTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductService productService;

    private Long testProductId;
    private int initialStockQuantity;

    @Test
    @DisplayName("동시에 재고 감소 테스트 - 성공")
    void testConcurrentDecreaseStock() throws InterruptedException {
        // Given

        Product product = Product.builder()
                .name("테스트 상품")
                .price(1000.0)
                .build();
        Product savedProduct = productService.saveProduct(product);

        this.testProductId = savedProduct.getId();
        this.initialStockQuantity = 100;

        Stock stock = Stock.builder()
                .productId(testProductId)
                .quantity(initialStockQuantity)
                .build();
        stockService.save(stock);

        int threadCount = 20; // 동시 사용자 수
        int decreaseQuantity = 2; // 각 요청에서 감소할 수량
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    ProductUpdateDto updateDto = new ProductUpdateDto(testProductId, -decreaseQuantity);
                    stockService.adjust(updateDto);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기
        executorService.shutdown();

        // THEN
        StockDto stockDto = stockService.getStock(testProductId);
        assertThat(stockDto.quantity()).isEqualTo(
                initialStockQuantity - (decreaseQuantity * threadCount)
        ); // 예상 감소량만큼 정확히 감소했는지 검증
    }

    @Test
    @DisplayName("동시에 재고 감소 테스트 - 재고 부족 예외")
    void testConcurrentDecreaseStock_withInsufficientStock() throws InterruptedException {
        // Given
        int threadCount = 30; // 요청 수는 재고보다 더 많도록
        int decreaseQuantity = 3; // 각 요청에서 감소할 수량
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    ProductUpdateDto updateDto = new ProductUpdateDto(testProductId, -decreaseQuantity);
                    stockService.adjust(updateDto);
                } catch (DomainException e) {
                    System.err.println("재고 부족 예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기
        executorService.shutdown();

        // THEN
        StockDto stock = stockService.getStock(testProductId);
        assertThat(stock.quantity()).isGreaterThanOrEqualTo(0); // 재고는 0 이상이어야 함
    }
}