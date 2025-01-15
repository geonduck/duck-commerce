package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.order.OrderErrorCode;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.dto.OrderCalculationResult;
import kr.hhplus.be.server.domain.order.dto.OrderItemRequest;
import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
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

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new DomainException(OrderErrorCode.NOT_FIND_EXCEPTION));
    }

    @Transactional
    public OrderResponse createOrder(String userId, List<OrderItemRequest> items) {
        // 1. 상품 재고 확인 및 차감
        List<ProductDomainDto> products = validateAndUpdateStock(items);

        // 2. 총 주문 금액 계산
        OrderCalculationResult calculationResult = OrderCalculationResult.calculateOrderItemsAndTotalAmount(items, products, userId);
        double totalAmount = calculationResult.totalAmount();
        List<OrderItem> orderItems = calculationResult.orderItems();

        // 3. 주문 생성 및 저장
        Order order = Order.builder().userId(userId).totalAmount(totalAmount).discountAmount(0.0) // 쿠폰 적용 전 초기값
                .orderStatus(OrderStatus.PENDING).build();
        Order savedOrder = orderRepository.save(order);

        // 4. 주문 아이템 저장
        List<OrderItem> saveOrderItems = saveOrderItems(savedOrder.getId(), orderItems);

        // 응답 생성
        return buildOrderResponse(savedOrder, saveOrderItems.stream().map(item -> new OrderItemResponse(item.getProductId(), item.getProductName(), item.getAmount())).collect(Collectors.toList()));
    }

    private List<OrderItem> saveOrderItems(Long id, List<OrderItem> orderItems) {
        orderItems.forEach(item -> OrderItem.builder().orderId(id).productId(item.getProductId()).userId(item.getUserId()).amount(item.getAmount()).productName(item.getProductName()).price(item.getPrice()).build());
        return orderItemRepository.saveAll(orderItems);
    }

    @Transactional
    public OrderResponse applyDiscountToOrder(String userId, Long orderId, Long couponAssignmentId) {
        // 1. 주문 조회
        Order order = findOrderById(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new DomainException(OrderErrorCode.USER_MISMATCH_EXCEPTION);
        }

        // 2. 쿠폰 사용 및 할인 적용
        CouponAssignmentDto couponAssignment = couponService.useCoupon(couponAssignmentId, userId);
        double discountAmount = couponAssignment.coupon().discountAmount();

        // 3. 주문 금액 업데이트
        double finalAmount = order.getTotalAmount() - discountAmount;
        order.applyDiscount(discountAmount, finalAmount);

        // 4. 업데이트 후 저장
        Order updatedOrder = orderRepository.save(order);

        // 5. 응답 생성
        List<OrderItemResponse> orderItems = getOrderItems(orderId);

        return buildOrderResponse(updatedOrder, orderItems);
    }

    private List<ProductDomainDto> validateAndUpdateStock(List<OrderItemRequest> items) {
        return items.stream().map(item -> {
            ProductDomainDto product = productService.getProduct(item.productId());
            item.validate(product);
            productService.updateProduct(new ProductUpdateDto(item.productId(), -item.amount())); // 재고 차감
            return product;
        }).collect(Collectors.toList());
    }

    private OrderResponse buildOrderResponse(Order order, List<OrderItemResponse> orderItems) {
        return OrderResponse.of(order, orderItems);
    }

    public List<OrderItemResponse> getOrderItems(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderItems.stream().map(item -> new OrderItemResponse(item.getProductId(), item.getProductName(), item.getAmount())).collect(Collectors.toList());
    }
}
