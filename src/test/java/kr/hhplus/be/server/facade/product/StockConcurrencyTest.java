package kr.hhplus.be.server.facade.product;

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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

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
    @DisplayName("동시에 재고 감소 및 조회 테스트")
    void testConcurrentDecreaseAndGetStock() throws InterruptedException {
        // Given
        Product product = Product.builder()
                .name("Test Product")
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

        int threadCount = 5; // Number of concurrent users
        int decreaseQuantity = 2; // Quantity to decrease per request
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CompletableFuture<?>[] decreaseFutures = IntStream.range(0, threadCount / 2)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        ProductUpdateDto updateDto = new ProductUpdateDto(testProductId, -decreaseQuantity);
                        stockService.adjust(updateDto);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Exception: " + e.getMessage());
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);

        CompletableFuture<?>[] getFutures = IntStream.range(0, threadCount / 2)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        StockDto stockDto = stockService.getStock(testProductId);
                        System.out.println("Current stock: " + stockDto.quantity());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Exception: " + e.getMessage());
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(decreaseFutures).join(); // Wait for all decrease operations to complete
        CompletableFuture.allOf(getFutures).join(); // Wait for all get operations to complete
        executorService.shutdown();

        // THEN
        StockDto stockDto = stockService.getStock(testProductId);
        assertThat(stockDto.quantity()).isEqualTo(
                initialStockQuantity - (decreaseQuantity * (threadCount / 2))
        ); // Verify that the stock decreased by the expected amount
    }

    @Test
    @DisplayName("낙관적 락 - 동시 재고 감소 테스트")
    void testConcurrentDecreaseWithOptimisticLock() throws InterruptedException {
        // Given
        Product product = Product.builder().name("테스트 상품").price(1000.0).build();
        Product savedProduct = productService.saveProduct(product);

        this.testProductId = savedProduct.getId();
        this.initialStockQuantity = 100;

        Stock stock = Stock.builder()
                .productId(testProductId)
                .quantity(initialStockQuantity)
                .build();
        stockService.save(stock);

        StockDto dto = stockService.getStock(testProductId);
        assertThat(dto.quantity()).isEqualTo(initialStockQuantity);

        int threadCount = 5;
        int decreaseQuantity = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CompletableFuture<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        ProductUpdateDto updateDto = new ProductUpdateDto(testProductId, -decreaseQuantity);
                        stockService.adjust(updateDto);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Exception: " + e.getMessage());
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        executorService.shutdown();

        StockDto stockDto = stockService.getStock(testProductId);
        assertThat(stockDto.quantity()).isEqualTo(
                initialStockQuantity - (decreaseQuantity * threadCount)
        );
    }
}