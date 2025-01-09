package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;
import kr.hhplus.be.server.domain.order.dto.OrderItemRequest;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.dto.StockDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final CouponService couponService;

    @Transactional
    public OrderResponse createOrder(String userId, List<OrderItemRequest> items, Long couponAssignmentId) {
        // 1. 쿠폰 유효성 검증 및 사용 처리
        double discountAmount = 0;
        if (couponAssignmentId != null) {
            CouponAssignment couponAssignment = couponService.useCoupon(couponAssignmentId, userId);
            discountAmount = couponAssignment.getCoupon().getDiscountAmount();
        }

        // 2. 상품 정보 조회 및 재고 확인
        List<ProductDomainDto> products = items.stream()
                .map(item -> {
                    ProductDomainDto product = productService.getProduct(item.productId());
                    productService.updateProduct(item.productId(), -item.amount()); // 재고 차감
                    return product;
                })
                .collect(Collectors.toList());

        // 3. 총 주문 금액 계산
        double totalAmount = products.stream()
                .mapToDouble(product -> {
                    int amount = items.stream()
                            .filter(item -> item.productId().equals(product.id()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Invalid product ID"))
                            .amount();
                    return product.price() * amount;
                })
                .sum();

        // 4. 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .assignmentId(couponAssignmentId)
                .totalAmount(totalAmount - discountAmount)
                .discountAmount(discountAmount)
                .orderStatus(Order.OrderStatus.PENDING)
                .build();
        Order saveOrder = orderRepository.save(order);

        // 5. 주문 아이템 생성 및 저장
        List<OrderItem> orderItems = items.stream()
                .map(item -> OrderItem.builder()
                        .orderId(saveOrder.getId())
                        .productId(item.productId())
                        .userId(userId)
                        .amount(item.amount())
                        .build())
                .collect(Collectors.toList());
        orderItemRepository.saveAll(orderItems);

        // 6. 응답 DTO 생성
        List<OrderItemResponse> orderItemResponses = orderItems.stream()
                .map(item -> {
                    ProductDomainDto product = products.stream()
                            .filter(p -> p.id().equals(item.getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Product not found for OrderItem"));
                    return new OrderItemResponse(item.getProductId(), product.name(), item.getAmount());
                })
                .collect(Collectors.toList());

        return OrderResponse.of(saveOrder, orderItemResponses);
    }


}
