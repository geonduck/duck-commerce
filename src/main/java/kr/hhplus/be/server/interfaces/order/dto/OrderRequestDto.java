package kr.hhplus.be.server.interfaces.order.dto;

import java.util.List;

public record OrderRequestDto (
        String userId,
        List<OrderItemRequestDto> orderItems,
        Long couponId
){
}
