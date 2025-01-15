package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.coupon.CouponStatus;
import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;
import kr.hhplus.be.server.domain.order.dto.OrderItemRequest;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CouponService couponService;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("재고 부족으로 주문 실패")
    void createOrderFailDueToStock() {
        // Given
        String userId = "testUser";
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 5)
        );

        ProductDomainDto product = new ProductDomainDto(1L, "상품 A", 100.0, 2);

        // Mock 상품 조회
        when(productService.getProduct(1L)).thenReturn(product);

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () ->
                orderService.createOrder(userId, items)
        );

        assertEquals("재고가 부족합니다", exception.getMessage());

        // Verify 호출 여부
        verify(productService, never()).updateProduct(any(ProductUpdateDto.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("쿠폰 없이 주문 생성 테스트")
    void createOrderWithoutCoupon() {
        // Given
        String userId = "testUser";
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 2),
                new OrderItemRequest(2L, 1)
        );

        ProductDomainDto product1 = new ProductDomainDto(1L, "상품 A", 100.0, 3);
        ProductDomainDto product2 = new ProductDomainDto(2L, "상품 B", 200.0, 2);

        // Mock 상품 조회
        when(productService.getProduct(1L)).thenReturn(product1);
        when(productService.getProduct(2L)).thenReturn(product2);

        // Mock 재고 차감
        doNothing().when(productService).updateProduct(new ProductUpdateDto(1L, -2));
        doNothing().when(productService).updateProduct(new ProductUpdateDto(2L, -1));

        // Mock 주문 저장
        Order mockOrder = Order.builder()
                .id(1L)
                .userId(userId)
                .totalAmount(400.0)
                .discountAmount(0.0)
                .orderStatus(OrderStatus.PENDING)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Mock 주문 아이템 저장
        List<OrderItem> savedItems = items.stream()
                .map(item -> OrderItem.builder()
                        .orderId(mockOrder.getId())
                        .productId(item.productId())
                        .userId(userId)
                        .amount(item.amount())
                        .productName(item.productId() == 1L ? "상품 A" : "상품 B")
                        .price(item.productId() == 1L ? product1.price() : product2.price())
                        .build()
                ).toList();
        when(orderItemRepository.saveAll(anyList())).thenReturn(savedItems);

        // When
        OrderResponse response = orderService.createOrder(userId, items);

        // Then
        assertEquals(1L, response.orderId());
        assertEquals(userId, response.userId());
        assertEquals(400.0, response.totalAmount());
        assertEquals(0.0, response.discountAmount());
        assertEquals(OrderStatus.PENDING, response.orderStatus());
        assertEquals(2, response.orderItems().size());

        // Verify 호출 여부
        verify(productService).getProduct(1L);
        verify(productService).getProduct(2L);
        verify(productService).updateProduct(new ProductUpdateDto(1L, -2));
        verify(productService).updateProduct(new ProductUpdateDto(2L, -1));
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("쿠폰을 사용하여 주문 할인 적용 테스트")
    void applyCouponToOrder() {
        // Given
        String userId = "testUser";
        Long orderId = 1L;
        Long couponAssignmentId = 100L;

        // 주문 저장 Mock
        Order existingOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .totalAmount(400.0)  // 쿠폰 적용 전 금액
                .discountAmount(0.0) // 할인 초기값
                .orderStatus(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(existingOrder));

        // 쿠폰 Mock
        Coupon coupon = new Coupon(1L, "10% 할인 쿠폰", 50.0, 8, LocalDateTime.now().plusDays(7));
        CouponAssignment couponAssignment = new CouponAssignment(couponAssignmentId, userId, coupon, CouponStatus.ISSUED);
        CouponAssignmentDto couponAssignmentDto = CouponAssignmentDto.of(couponAssignment);

        when(couponService.useCoupon(couponAssignmentId, userId)).thenReturn(couponAssignmentDto);

        // 저장 후의 주문 Mock
        Order updatedOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .totalAmount(350.0)  // 쿠폰 적용 후 금액
                .discountAmount(50.0) // 할인 금액
                .orderStatus(OrderStatus.PENDING)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        // Mock 주문 아이템 조회
        OrderItem item1 = OrderItem.builder()
                .orderId(orderId)
                .productId(1L)
                .userId(userId)
                .amount(2)
                .productName("상품 A")
                .price(100.0)
                .build();

        OrderItem item2 = OrderItem.builder()
                .orderId(orderId)
                .productId(2L)
                .userId(userId)
                .amount(1)
                .productName("상품 B")
                .price(200.0)
                .build();

        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(item1, item2));

        // When
        OrderResponse response = orderService.applyDiscountToOrder(userId, orderId, couponAssignmentId);

        // Then
        // 1. 응답 검증
        assertEquals(orderId, response.orderId());
        assertEquals(userId, response.userId());
        assertEquals(350.0, response.totalAmount());   // 할인 적용된 총 금액
        assertEquals(50.0, response.discountAmount()); // 할인 금액
        assertEquals(2, response.orderItems().size()); // 주문 아이템 수 확인

        // 각 아이템의 정보가 정확한지 확인
        assertEquals("상품 A", response.orderItems().get(0).productName());
        assertEquals(2, response.orderItems().get(0).amount());
        assertEquals("상품 B", response.orderItems().get(1).productName());
        assertEquals(1, response.orderItems().get(1).amount());

        // 2. Verify 호출 검증
        verify(orderRepository).findById(orderId);     // 주문 조회
        verify(couponService).useCoupon(couponAssignmentId, userId); // 쿠폰 사용
        verify(orderRepository).save(any(Order.class)); // 주문 업데이트
        verify(orderItemRepository).findByOrderId(orderId); // 아이템 조회
    }

}
