package kr.hhplus.be.server.facade.product;

import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
import kr.hhplus.be.server.domain.product.dto.StockDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    private Long productId;

    @BeforeEach
    void setup() {
        Product product = Product.builder()
                .name("Test Product")
                .price(100D)
                .build();
        product = productService.saveProduct(product);
        productId = product.getId();

        Stock stock = Stock.builder()
                .productId(product.getId())
                .quantity(10)
                .build();
        stockService.save(stock);
    }

    @Test
    @DisplayName("재고 업데이트 테스트")
    void testConcurrentStockUpdate() {
        // Given
        int threadCount = 20;
        ProductUpdateDto updateRequest = new ProductUpdateDto(
                productId,
                1
        );

        List<String> results = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        StockDto stockDto = stockService.adjust(updateRequest);
                        results.add("Success");
                        results.add(String.valueOf(stockDto));
                    } catch (Exception e) {
                        results.add("Failed: " + e.getMessage());
                    }
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        long successCount = results.stream().filter(r -> r.equals("Success")).count();
        long failureCount = results.stream().filter(r -> r.startsWith("Failed")).count();

        System.out.println("Results: " + results);
        assertThat(successCount).isEqualTo(threadCount);
        assertThat(failureCount).isEqualTo(0);

        Stock finalStock = stockService.findStockByProductId(productId);
        assertThat(finalStock.getQuantity()).isEqualTo(10 + threadCount);
    }

    @Test
    @DisplayName("재고 차감 테스트 : 재고가 부족한 경우 차감되지 않아 재고가 음수가 되지 않음")
    void testConcurrentStockDecrease() {
        // Given
        int threadCount = 20;

        ProductUpdateDto updateRequest = new ProductUpdateDto(
                productId,
                -1
        );
        int expectedQuantity = stockService.getStock(productId).quantity();

        List<String> results = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        StockDto stockDto = stockService.adjust(updateRequest);
                        results.add("Success");
                        results.add(String.valueOf(stockDto));
                    } catch (Exception e) {
                        results.add("Failed: " + e.getMessage());
                    }
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        long successCount = results.stream().filter(r -> r.equals("Success")).count();
        long failureCount = results.stream().filter(r -> r.startsWith("Failed")).count();

        System.out.println("Results: " + results);

        assertThat(successCount).isEqualTo(expectedQuantity);
        assertThat(failureCount).isEqualTo(threadCount - expectedQuantity);

        Stock finalStock = stockService.findStockByProductId(productId);
        assertThat(finalStock.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("조회 테스트")
    void testConcurrentStockRetrieval() {
        // Given
        int threadCount = 20;

        List<StockDto> results = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        StockDto stockDto = stockService.getStock(productId);
                        results.add(stockDto);
                    } catch (Exception e) {
                        results.add(null);
                    }
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        long successCount = results.stream().filter(r -> r != null).count();
        long failureCount = results.stream().filter(r -> r == null).count();

        System.out.println("Results: " + results);
        assertThat(successCount).isEqualTo(threadCount);
        assertThat(failureCount).isEqualTo(0);

        int expectedQuantity = stockService.getStock(productId).quantity();
        results.forEach(stockDto -> assertThat(stockDto.quantity()).isEqualTo(expectedQuantity));
    }

    @Test
    @DisplayName("동시에 재고 변화 및 조회 테스트")
    void testComprehensiveConcurrentStockOperations() {
        // Given
        int threadCount = 30; // Number of concurrent users
        int decreaseThreads = 10;
        int increaseThreads = 10;
        int retrieveThreads = 10;

        List<String> results = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<?>[] futures = new CompletableFuture<?>[threadCount];

        // Decrease stock threads
        for (int i = 0; i < decreaseThreads; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    ProductUpdateDto updateRequest = new ProductUpdateDto(productId, -1);
                    StockDto stockDto = stockService.adjust(updateRequest);
                    results.add("Decrease Success: " + stockDto);
                } catch (Exception e) {
                    results.add("Decrease Failed: " + e.getMessage());
                }
            });
        }

        // Increase stock threads
        for (int i = decreaseThreads; i < decreaseThreads + increaseThreads; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    ProductUpdateDto updateRequest = new ProductUpdateDto(productId, 1);
                    StockDto stockDto = stockService.adjust(updateRequest);
                    results.add("Increase Success: " + stockDto);
                } catch (Exception e) {
                    results.add("Increase Failed: " + e.getMessage());
                }
            });
        }

        // Retrieve stock threads
        for (int i = decreaseThreads + increaseThreads; i < threadCount; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    StockDto stockDto = stockService.getStock(productId);
                    results.add("Retrieve Success: " + stockDto);
                } catch (Exception e) {
                    results.add("Retrieve Failed: " + e.getMessage());
                }
            });
        }

        CompletableFuture.allOf(futures).join(); // Wait for all tasks to complete

        // THEN validate results
        long decreaseSuccessCount = results.stream().filter(r -> r.startsWith("Decrease Success")).count();
        long increaseSuccessCount = results.stream().filter(r -> r.startsWith("Increase Success")).count();
        long retrieveSuccessCount = results.stream().filter(r -> r.startsWith("Retrieve Success")).count();
        long failureCount = results.stream().filter(r -> r.contains("Failed")).count();

        System.out.println("Results: " + results);
        assertThat(decreaseSuccessCount).isEqualTo(decreaseThreads);
        assertThat(increaseSuccessCount).isEqualTo(increaseThreads);
        assertThat(retrieveSuccessCount).isEqualTo(retrieveThreads);
        assertThat(failureCount).isEqualTo(0);

        // Validate final stock quantity
        Stock finalStock = stockService.findStockByProductId(productId);
        int expectedFinalQuantity = 10 - decreaseThreads + increaseThreads; // Initial stock - decreases + increases
        assertThat(finalStock.getQuantity()).isEqualTo(expectedFinalQuantity);
    }
}