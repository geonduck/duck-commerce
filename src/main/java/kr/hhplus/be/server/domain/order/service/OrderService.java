package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.order.OrderErrorCode;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.dto.OrderCalculationResult;
import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
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
    /**
     * 주문 생성 로직 - 재고 확인/차감 및 총 주문 금액 계산은 Facade에서 수행하도록 변경
     */
    @Transactional
    public OrderResponse createOrder(String userId, OrderCalculationResult calculationResult) {
        double totalAmount = calculationResult.totalAmount();
        List<OrderItem> orderItems = calculationResult.orderItems();

        // 주문 생성 및 저장
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .discountAmount(0.0) // 쿠폰 적용 전 초기값
                .orderStatus(OrderStatus.PENDING)
                .build();
        Order savedOrder = orderRepository.save(order);

        // 주문 아이템 저장
        saveOrderItems(savedOrder.getId(), orderItems);

        // 응답 생성
        return buildOrderResponse(savedOrder, orderItems.stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getProductName(), item.getAmount()))
                .collect(Collectors.toList()));
    }

    /**
     * 주문 아이템 저장
     */
    private void saveOrderItems(Long orderId, List<OrderItem> orderItems) {
        orderItems.forEach(item -> OrderItem.builder()
                .orderId(orderId)
                .productId(item.getProductId())
                .userId(item.getUserId())
                .amount(item.getAmount())
                .productName(item.getProductName())
                .price(item.getPrice())
                .build());
        orderItemRepository.saveAll(orderItems);
    }

    /**
     * 쿠폰 적용 로직 - 쿠폰 사용 및 할인 금액 계산은 Facade에서 수행하도록 변경
     */
    @Transactional
    public OrderResponse applyDiscount(Order order, double discountAmount) {
        // 할인 적용
        double finalAmount = order.getTotalAmount() - discountAmount;
        order.applyDiscount(discountAmount, finalAmount);

        // 저장
        Order updatedOrder = orderRepository.save(order);

        // 응답 생성
        List<OrderItemResponse> orderItems = getOrderItems(order.getId());
        return buildOrderResponse(updatedOrder, orderItems);
    }

    /**
     * 주문 아이템 조회
     */
    public List<OrderItemResponse> getOrderItems(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderItems.stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getProductName(), item.getAmount()))
                .collect(Collectors.toList());
    }

    /**
     * 주문 엔티티로 응답 객체 생성
     */
    private OrderResponse buildOrderResponse(Order order, List<OrderItemResponse> orderItems) {
        return OrderResponse.of(order, orderItems);
    }

    /**
     * 특정 주문 ID로 주문 조회
     */
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException(OrderErrorCode.NOT_FIND_EXCEPTION));
    }

    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(order -> buildOrderResponse(order, getOrderItems(order.getId())))
                .collect(Collectors.toList());
    }
}
