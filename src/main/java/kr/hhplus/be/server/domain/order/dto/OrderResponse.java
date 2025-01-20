package kr.hhplus.be.server.domain.order.dto;

import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.entity.Order;

import java.util.List;

public record OrderResponse(
        Long orderId,
        String userId,
        Long couponId,
        double totalAmount,
        double discountAmount,
        OrderStatus orderStatus,
        List<OrderItemResponse> orderItems
) {
    public static OrderResponse of(Order order, List<OrderItemResponse> orderItemResponses) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getAssignmentId(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getOrderStatus(),
                orderItemResponses
        );
    }
}