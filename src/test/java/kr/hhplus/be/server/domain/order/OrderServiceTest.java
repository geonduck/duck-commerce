package kr.hhplus.be.server.domain.order;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.order.dto.OrderCalculationResult;
import kr.hhplus.be.server.domain.order.dto.OrderItemRequest;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class OrderServiceTest {

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
    @DisplayName("주문 생성 성공")
    void testCreateOrder_Success() {
        // Given
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

        // When
        OrderResponse response = orderService.createOrder(TEST_USER_ID, calculationResult);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(TEST_USER_ID);
        assertThat(response.orderItems()).hasSize(2);
        assertThat(response.totalAmount()).isEqualTo(400.0); // 상품 A 2개(200) + 상품 B 1개(200)

    }

    @Test
    @DisplayName("쿠폰 할인을 적용한 주문 성공")
    void testApplyDiscount_Success() {
        // Given
        List<OrderItemRequest> orderItems = List.of(
                new OrderItemRequest(productA.getId(), 2),
                new OrderItemRequest(productB.getId(), 1)
        );

        OrderCalculationResult calculationResult = OrderCalculationResult.calculateOrderItemsAndTotalAmount(
                orderItems,
                List.of(
                        new ProductDomainDto(productA.getId(), productA.getName(), productA.getPrice(), stockA.getQuantity()),
                        new ProductDomainDto(productB.getId(), productB.getName(), productB.getPrice(), stockB.getQuantity())
                ),
                TEST_USER_ID
        );

        OrderResponse createdOrder = orderService.createOrder(TEST_USER_ID, calculationResult);

        CouponAssignmentDto couponAssignment = couponService.assignCoupon(coupon.getId(), TEST_USER_ID);
        couponService.useCoupon(couponAssignment.id(), TEST_USER_ID);

        // When
        OrderResponse discountedOrder = orderService.applyDiscount(orderService.findOrderById(createdOrder.orderId()), 50.0);

        // Then
        assertThat(discountedOrder.discountAmount()).isEqualTo(50.0);
        assertThat(discountedOrder.totalAmount()).isEqualTo(350.0); // 400 - 50
    }

    @Test
    @DisplayName("존재하지 않는 주문을 조회 시도 시 예외 발생")
    void testFindInvalidOrder_Failure() {
        // When & Then
        assertThatThrownBy(() -> orderService.findOrderById(999L))
                .isInstanceOf(DomainException.class)
                .hasMessage("존재하지 않는 주문입니다");
    }

    @Test
    @DisplayName("주문 상태 업데이트 성공")
    void testUpdateOrderStatus_Success() {
        // Given
        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(productA.getId(), 2));
        OrderCalculationResult result = OrderCalculationResult.calculateOrderItemsAndTotalAmount(
                orderItems,
                List.of(new ProductDomainDto(productA.getId(), productA.getName(), productA.getPrice(), stockA.getQuantity())),
                TEST_USER_ID
        );

        OrderResponse orderResponse = orderService.createOrder(TEST_USER_ID, result);
        Long orderId = orderResponse.orderId();

        // When
        orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED);

        // Then
        Order updatedOrder = orderService.findOrderById(orderId);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}