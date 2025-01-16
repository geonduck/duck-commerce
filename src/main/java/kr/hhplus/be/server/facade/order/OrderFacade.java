package kr.hhplus.be.server.facade.order;

import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.order.dto.OrderCalculationResult;
import kr.hhplus.be.server.domain.order.dto.OrderItemRequest;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.interfaces.order.dto.OrderItemRequestDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderItemResponseDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderRequestDto;
import kr.hhplus.be.server.interfaces.order.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;

    /**
     * 주문 생성
     */
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        // 1. 상품 재고 확인 및 차감
        List<ProductDomainDto> products = validateAndUpdateStock(requestDto.orderItems());

        // 2. 총 주문 금액 계산
        OrderCalculationResult calculationResult = OrderCalculationResult.calculateOrderItemsAndTotalAmount(
                requestDto.orderItems().stream()
                        .map(item -> new OrderItemRequest(item.productId(), item.amount())) // 기본 수량 1로 처리
                        .collect(Collectors.toList()),
                products,
                requestDto.userId());

        // 3. 주문 생성
        OrderResponse orderResponse = orderService.createOrder(requestDto.userId(), calculationResult);

        // 4. 응답 DTO 반환
        return toOrderResponseDto(orderResponse);
    }

    /**
     * 쿠폰을 사용한 할인 적용
     */
    public OrderResponseDto applyDiscount(String userId, Long orderId, Long couponId) {
        // 1. 주문 정보 조회
        OrderResponse order = orderService.findByOrderId(orderId);

        // 2. 쿠폰 사용
        CouponAssignmentDto couponAssignment = couponService.useCoupon(couponId, userId);
        double discountAmount = couponAssignment.coupon().discountAmount();

        // 3. 할인 적용
        OrderResponse orderResponse = orderService.applyDiscount(order, discountAmount);

        // 4. 응답 DTO 반환
        return toOrderResponseDto(orderResponse);
    }

    /**
     * 상품 재고를 검증하고 재고 차감
     */
    private List<ProductDomainDto> validateAndUpdateStock(List<OrderItemRequestDto> orderItems) {
        return orderItems.stream()
                .map(item -> {
                    ProductDomainDto product = productService.getProduct(item.productId());
                    productService.updateProduct(new ProductUpdateDto(item.productId(), item.amount())); // 재고 차감
                    return product;
                }).collect(Collectors.toList());
    }

    /**
     * OrderResponse를 DTO로 변환
     */
    private OrderResponseDto toOrderResponseDto(OrderResponse response) {
        return new OrderResponseDto(
                response.userId(),
                response.orderId(),
                response.orderItems().stream()
                        .map(item -> new OrderItemResponseDto(
                                item.productId(),
                                item.productName(),
                                item.amount()
                        ))
                        .collect(Collectors.toList()),
                response.orderStatus().name(),
                response.totalAmount(),
                response.discountAmount()
        );
    }

    public List<OrderResponseDto> getOrdersByUserId(String userId) {

        // 1. 주문 데이터 조회 (OrderService 호출)
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);

        // 2. 응답 DTO 변환
        return orders.stream()
                .map(this::toOrderResponseDto)
                .collect(Collectors.toList());
    }
}