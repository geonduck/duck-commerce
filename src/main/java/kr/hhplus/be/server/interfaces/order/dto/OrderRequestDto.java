package kr.hhplus.be.server.interfaces.order.dto;

import java.util.List;

public record OrderRequestDto (
        String userId,
        List<Long> orderItems,
        Long couponId
){
}
