package kr.hhplus.be.server.interfaces.consumer;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.order.OrderEventStatus;
import kr.hhplus.be.server.domain.order.dto.OrderCalculationResult;
import kr.hhplus.be.server.domain.order.dto.OrderItemRequest;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.OrderOutbox;
import kr.hhplus.be.server.domain.order.service.OrderOutboxEventService;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.StockService;
import kr.hhplus.be.server.infrastructure.event.OrderEventSender;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Component
class OrderKafkaConnectionTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OrderOutboxEventService orderOutboxEventService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private CouponService couponService;

    private Product productA;
    private Product productB;
    private Stock stockA;
    private Stock stockB;
    private Coupon coupon;

    private static final String TEST_USER_ID = "testUser";

    @BeforeEach
    void setup() {
        // 상품 데이터 초기화
        productA = Product.builder()
                .name("상품 A")
                .price(100.0)
                .build();
        productB = Product.builder()
                .name("상품 B")
                .price(200.0)
                .build();
        productA = productService.saveProduct(productA);
        productB = productService.saveProduct(productB);

        // 재고 데이터 초기화
        stockA = Stock.builder()
                .productId(productA.getId())
                .quantity(10) // 초기 재고 10
                .build();
        stockB = Stock.builder()
                .productId(productB.getId())
                .quantity(5) // 초기 재고 5
                .build();
        stockA = stockService.save(stockA);
        stockB = stockService.save(stockB);

        // 쿠폰 데이터 초기화
        coupon = Coupon.builder()
                .name("할인 쿠폰")
                .discountAmount(50.0)
                .maxIssuance(10)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        coupon = couponService.save(coupon);
    }

    @Test
    void 테스트() throws InterruptedException {

        List<OrderItemRequest> orderItems = List.of(
                new OrderItemRequest(productA.getId(), 2), // 상품 A - 수량 2
                new OrderItemRequest(productB.getId(), 1)  // 상품 B - 수량 1
        );

        OrderCalculationResult calculationResult = OrderCalculationResult.calculateOrderItemsAndTotalAmount(
                orderItems,
                List.of(
                        new ProductDomainDto(productA.getId(), productA.getName(), productA.getPrice(), stockA.getQuantity()),
                        new ProductDomainDto(productB.getId(), productB.getName(), productB.getPrice(), stockB.getQuantity())
                ),
                TEST_USER_ID
        );

        OrderResponse orderResponse = orderService.createOrder(TEST_USER_ID, calculationResult);

        Long orderId = orderResponse.orderId();

        OrderOutbox orderOutbox = OrderOutbox.builder()
                .messageId(orderId)
                .topic("order-topic")
                .payload("{ \"orderId\": " + orderId + ", \"userId\": " + TEST_USER_ID + " }")
                .status(OrderEventStatus.NEW)
                .build();

        orderOutboxEventService.saveOutboxEvent(orderOutbox);

        kafkaTemplate.send("order-topic", String.valueOf(orderId), "{ \"orderId\": " + orderId + ", \"userId\": " + TEST_USER_ID + " }");

        Awaitility.await()
                .pollInterval(Duration.ofMillis(300)) // 300ms에 한 번씩 확인
                .atMost(10, TimeUnit.SECONDS) // 최대 10초까지 기다림
                .untilAsserted(() -> {
                    assertThat(orderOutboxEventService.findByMessageId(orderId).getStatus()).isEqualTo(OrderEventStatus.SENT);
                });
    }
}