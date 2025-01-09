package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;
import kr.hhplus.be.server.domain.order.dto.OrderItemRequest;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @DisplayName("쿠폰 없이 주문 생성 테스트")
    void createOrderWithoutCoupon() {
        // Given
        String userId = "testUser";
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 2), // productId=1, amount=2
                new OrderItemRequest(2L, 1)  // productId=2, amount=1
        );

        ProductDomainDto product1 = new ProductDomainDto(1L, "상품 A", 100.0, 3); // 가격 100.0
        ProductDomainDto product2 = new ProductDomainDto(2L, "상품 B", 200.0, 2); // 가격 200.0

        // Mock 상품 조회
        when(productService.getProduct(1L)).thenReturn(product1);
        when(productService.getProduct(2L)).thenReturn(product2);

        // Mock 재고 차감
        doNothing().when(productService).updateProduct(1L, -2);
        doNothing().when(productService).updateProduct(2L, -1);

        // Mock 주문 저장
        Order mockOrder = Order.builder()
                .id(1L)
                .userId(userId)
                .totalAmount(400.0) // 2 * 100 + 1 * 200
                .discountAmount(0.0)
                .orderStatus(Order.OrderStatus.PENDING)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Mock 주문 아이템 저장
        List<OrderItem> savedItems = items.stream().map(item -> OrderItem.builder()
                .id(item.productId()) // 간단히 productId를 ID로 설정
                .orderId(mockOrder.getId())
                .productId(item.productId())
                .userId(userId)
                .amount(item.amount())
                .build()
        ).collect(Collectors.toList());
        when(orderItemRepository.saveAll(anyList())).thenReturn(savedItems);

        // When
        OrderResponse response = orderService.createOrder(userId, items, null);

        // Then
        assertEquals(1L, response.orderId());
        assertEquals(userId, response.userId());
        assertEquals(400.0, response.totalAmount());
        assertEquals(0.0, response.discountAmount());
        assertEquals(Order.OrderStatus.PENDING, response.orderStatus());
        assertEquals(2, response.orderItems().size());

        // 상품명 확인
        assertEquals("상품 A", response.orderItems().get(0).productName());
        assertEquals("상품 B", response.orderItems().get(1).productName());

        // Verify 호출 여부
        verify(productService).getProduct(1L);
        verify(productService).getProduct(2L);
        verify(productService).updateProduct(1L, -2);
        verify(productService).updateProduct(2L, -1);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("쿠폰 적용된 주문 생성 테스트")
    void createOrderWithCoupon() {
        // Given
        String userId = "testUser";
        Long couponAssignmentId = 100L;
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 2), // productId=1, amount=2
                new OrderItemRequest(2L, 1)  // productId=2, amount=1
        );

        ProductDomainDto product1 = new ProductDomainDto(1L, "상품 A", 100.0, 4); // 가격 100.0
        ProductDomainDto product2 = new ProductDomainDto(2L, "상품 B", 200.0, 5); // 가격 200.0

        // Mock 쿠폰 조회 및 사용
        CouponAssignment couponAssignment = CouponAssignment.builder()
                .id(couponAssignmentId)
                .userId(userId)
                .coupon(Coupon.builder()
                        .id(1L)
                        .name("test")
                        .discountAmount(50)
                        .maxIssuance(10)
                        .expiredAt(LocalDateTime.now().plusDays(7))
                        .build()) // 할인금액 50
                .build();
        when(couponService.useCoupon(couponAssignmentId, userId)).thenReturn(couponAssignment);

        // Mock 상품 조회
        when(productService.getProduct(1L)).thenReturn(product1);
        when(productService.getProduct(2L)).thenReturn(product2);

        // Mock 재고 차감
        doNothing().when(productService).updateProduct(1L, -2);
        doNothing().when(productService).updateProduct(2L, -1);

        // Mock 주문 저장
        Order mockOrder = Order.builder()
                .id(1L)
                .userId(userId)
                .totalAmount(350.0) // (2 * 100 + 1 * 200) - 50
                .discountAmount(50.0)
                .orderStatus(Order.OrderStatus.PENDING)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Mock 주문 아이템 저장
        List<OrderItem> savedItems = items.stream().map(item -> OrderItem.builder()
                .id(item.productId()) // 간단히 productId를 ID로 설정
                .orderId(mockOrder.getId())
                .productId(item.productId())
                .userId(userId)
                .amount(item.amount())
                .build()
        ).collect(Collectors.toList());
        when(orderItemRepository.saveAll(anyList())).thenReturn(savedItems);

        // When
        OrderResponse response = orderService.createOrder(userId, items, couponAssignmentId);

        // Then
        assertEquals(1L, response.orderId());
        assertEquals(userId, response.userId());
        assertEquals(350.0, response.totalAmount());
        assertEquals(50.0, response.discountAmount());
        assertEquals(Order.OrderStatus.PENDING, response.orderStatus());
        assertEquals(2, response.orderItems().size());

        // 상품명 확인
        assertEquals("상품 A", response.orderItems().get(0).productName());
        assertEquals("상품 B", response.orderItems().get(1).productName());

        // Verify 호출 여부
        verify(couponService).useCoupon(couponAssignmentId, userId);
        verify(productService).getProduct(1L);
        verify(productService).getProduct(2L);
        verify(productService).updateProduct(1L, -2);
        verify(productService).updateProduct(2L, -1);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
    }
}
