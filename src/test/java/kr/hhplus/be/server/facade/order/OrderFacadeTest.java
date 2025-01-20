package kr.hhplus.be.server.facade.order;

import kr.hhplus.be.server.domain.coupon.CouponStatus;
import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.dto.CouponDto;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.interfaces.order.dto.OrderItemRequestDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderRequestDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private CouponService couponService;

    @InjectMocks
    private OrderFacade orderFacade;

    @Test
    @DisplayName("주문 생성 테스트")
    void createOrderTest() {
        // Given
        String userId = "testUser";
        OrderRequestDto requestDto = new OrderRequestDto(
                userId,
                List.of(new OrderItemRequestDto(1L, 2), new OrderItemRequestDto(2L, 1)),
                null
        );

        // Mock product service
        when(productService.getProduct(1L)).thenReturn(new ProductDomainDto(1L, "상품 A", 100.0, 10));
        when(productService.getProduct(2L)).thenReturn(new ProductDomainDto(2L, "상품 B", 200.0, 5));

        // Mock order service
        when(orderService.createOrder(any(), any())).thenReturn(
                new OrderResponse(1L, userId, 1L, 400.0, 0.0, OrderStatus.PENDING, List.of())
        );

        // When
        OrderResponseDto responseDto = orderFacade.createOrder(requestDto);

        // Then
        assertEquals(userId, responseDto.userId());
        assertEquals(400.0, responseDto.totalPrice());
        assertEquals(0.0, responseDto.discountPrice());

        verify(productService, times(2)).getProduct(any());
        verify(orderService).createOrder(any(), any());
    }

    @Test
    @DisplayName("쿠폰 적용 테스트")
    void applyDiscountTest() {
        // Given
        String userId = "testUser";
        Long orderId = 1L;
        Long couponId = 100L;

        // Mock coupon service
        Coupon coupon = new Coupon(1L, "특별 할인", 50.0, 10, LocalDateTime.now().plusDays(7));
        CouponAssignmentDto couponAssignmentDto = new CouponAssignmentDto(couponId, userId, CouponStatus.ISSUED.name(), CouponDto.of(coupon));
        when(couponService.useCoupon(couponId, userId)).thenReturn(couponAssignmentDto);

        // Mock order service
        OrderResponse order = new OrderResponse(orderId, userId, 1L, 400.0, 0.0, OrderStatus.PENDING, List.of());
        when(orderService.findByOrderId(orderId)).thenReturn(order);
        when(orderService.applyDiscount(order, 50)).thenReturn(
                new OrderResponse(orderId, userId, 1L, 350.0, 50.0, OrderStatus.PENDING, List.of())
        );
        OrderRequestDto requestDto = new OrderRequestDto(
                userId,
                List.of(new OrderItemRequestDto(1L, 2), new OrderItemRequestDto(2L, 1)),
                couponId
        );

        // When
        OrderResponseDto responseDto = orderFacade.applyDiscount(orderId, requestDto);

        // Then
        assertEquals(350.0, responseDto.totalPrice());
        assertEquals(50.0, responseDto.discountPrice());

        verify(couponService).useCoupon(couponId, userId);
        verify(orderService).applyDiscount(order, 50);
    }
}