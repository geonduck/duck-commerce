package kr.hhplus.be.server.domain.order.dto;

public record OrderItemRequest(Long productId, int amount) {}