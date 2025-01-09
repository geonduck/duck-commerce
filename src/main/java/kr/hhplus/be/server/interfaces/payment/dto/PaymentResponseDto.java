package kr.hhplus.be.server.interfaces.payment.dto;

public record PaymentResponseDto (
        String userId,
        int orderId,
        String status

){
}
