package kr.hhplus.be.server.interfaces.order.dto;

public record OrderResponseDto (
        String userId,
        int orderId,
        String status

){
}
