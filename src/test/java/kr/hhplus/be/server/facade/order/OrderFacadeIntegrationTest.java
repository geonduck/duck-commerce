package kr.hhplus.be.server.facade.order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;

@SpringBootTest
@Transactional
public class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private CouponService couponService;

    private Coupon coupon;

    private static final String TEST_USER_ID = "testUser";

    private Long productAId;
    private Long productBId;

    private Long assignmentId;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setup() {

        // 데이터베이스 초기화
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
        entityManager.flush();

        // 상품 데이터 초기화
        Product productA = Product.builder()
                .name("상품 A")
                .price(100.0)
                .build();
        Product productB = Product.builder()
                .name("상품 B")
                .price(200.0)
                .build();
        productA = productService.saveProduct(productA);
        productB = productService.saveProduct(productB);

        productAId = productA.getId();
        productBId = productB.getId();

        // 재고 데이터 초기화
        // 초기 재고 10
        Stock stockA = Stock.builder()
                .productId(productA.getId())
                .quantity(10) // 초기 재고 10
                .build();
        // 초기 재고 5
        Stock stockB = Stock.builder()
                .productId(productB.getId())
                .quantity(5) // 초기 재고 5
                .build();
        stockA = stockService.save(stockA);
        stockB = stockService.save(stockB);

        // 쿠폰 데이터 초기화
        coupon = Coupon.builder()
                .name("할인 쿠폰")
                .discountAmount(100.0)
                .maxIssuance(10)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        coupon = couponService.save(coupon);
        CouponAssignmentDto dto = couponService.assignCoupon(coupon.getId(), TEST_USER_ID);
        assignmentId = dto.id();

    }

    @Test
    @DisplayName("주문 성공")
    void testCreateOrder_success() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto(
                TEST_USER_ID,
                List.of(new OrderItemRequestDto(productAId, 2), new OrderItemRequestDto(productBId, 3)), // 상품 2개 대상 요청
                null
        );

        // When
        OrderResponseDto responseDto = orderFacade.createOrder(requestDto);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.userId()).isEqualTo(TEST_USER_ID);

        // 상품 재고 감소 확인
        ProductDomainDto productA = productService.getProduct(productAId);
        ProductDomainDto productB = productService.getProduct(productBId);

        assertThat(productA.stockQuantity()).isEqualTo(8); // 남은 재고: 10 - 2
        assertThat(productB.stockQuantity()).isEqualTo(2); // 남은 재고: 5 - 3

        // 총 주문 금액 검증
        assertThat(responseDto.totalPrice()).isEqualTo(800.0); // (2 * 100.0) + (3 * 200.0)
    }

    @Test
    @DisplayName("주문 쿠폰 할인 적용 성공")
    void testApplyDiscount_success() {
        // Given
        // 주문 생성
        OrderRequestDto requestDto = new OrderRequestDto(
                TEST_USER_ID,
                List.of(new OrderItemRequestDto(productAId, 2), new OrderItemRequestDto(productBId, 1)),
                assignmentId
        );
        // When: 쿠폰 적용
        OrderResponseDto orderResponse = orderFacade.createOrder(requestDto);

        // Then
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.totalPrice()).isEqualTo(300.0); // 전체 금액: 400.0 - 100.0
        assertThat(orderResponse.discountPrice()).isEqualTo(100.0); // 할인 금액
    }

    @Test
    @DisplayName("사용자 아이디로 주문 조회 성공")
    void testGetOrdersByUserId() {
        // Given
        // 주문 생성
        orderFacade.createOrder(new OrderRequestDto(TEST_USER_ID, List.of(new OrderItemRequestDto(productAId, 1)), null));
        orderFacade.createOrder(new OrderRequestDto(TEST_USER_ID, List.of(new OrderItemRequestDto(productBId, 2)), null));

        // When
        List<OrderResponseDto> orders = orderFacade.getOrdersByUserId(TEST_USER_ID);

        // Then
        assertThat(orders).hasSize(2); // 두 개의 주문을 생성했으므로 사이즈 2
        assertThat(orders.get(0).userId()).isEqualTo(TEST_USER_ID);
        assertThat(orders.get(1).userId()).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("재고가 부족한 주문 실패")
    void testCreateOrder_withInsufficientStock() {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto(
                TEST_USER_ID,
                List.of(new OrderItemRequestDto(productAId, 20)), // 재고 초과 요청
                null
        );

        // When & Then
        assertThatThrownBy(() -> orderFacade.createOrder(requestDto))
                .isInstanceOf(RuntimeException.class) // 특정 예외 클래스로 교체 가능
                .hasMessageContaining("재고가 부족합니다");
    }

}