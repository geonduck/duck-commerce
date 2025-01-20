package kr.hhplus.be.server.facade.order;

import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.StockService;
import kr.hhplus.be.server.interfaces.order.dto.OrderItemRequestDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderRequestDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    private static final String TEST_USER_ID = "testUser";

    @BeforeEach
    void setup() {
        // 상품, 재고 초기화
        Product product = Product.builder()
                .name("Test Product")
                .price(100D)
                .build();
        product = productService.saveProduct(product);

        Stock stock = Stock.builder()
                .productId(product.getId())
                .quantity(10) // 초기 재고 10개
                .build();
        stockService.save(stock);
    }

    @Test
    @DisplayName("동시 주문 생성 테스트")
    void testConcurrentOrderCreation() throws InterruptedException {
        // Given
        Long productId = 1L; // 미리 저장된 상품 ID
        int threadCount = 20; // 동시 사용자 수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        OrderRequestDto orderRequest = new OrderRequestDto(
                TEST_USER_ID,
                List.of(new OrderItemRequestDto(productId, 1)), // 상품 1개씩 주문
                null
        );

        // Shared result container for validation
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    // Order 생성 요청
                    OrderResponseDto response = orderFacade.createOrder(orderRequest);
                    results.add("Success: " + response.orderId());
                } catch (Exception e) {
                    results.add("Failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기
        executorService.shutdown();

        // THEN 결과 검증
        long successCount = results.stream().filter(r -> r.startsWith("Success")).count();
        long failureCount = results.stream().filter(r -> r.startsWith("Failed")).count();

        System.out.println("Results: " + results);
        // 최대 성공한 주문 갯수는 재고 수량과 같아야 합니다.
        assertThat(successCount).isEqualTo(10); // 초기 재고 10개
        assertThat(failureCount).isEqualTo(10); // 실패 요청 갯수는 남은 스레드 수만큼
    }
}