package kr.hhplus.be.server.interfaces.order.dto;

import java.util.List;

public record OrderResponseDto (
        String userId,
        Long orderId,
        List<OrderItemResponseDto> orderItems,
        String status,
        double totalPrice,
        double discountPrice

){
}
