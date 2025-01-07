package kr.hhplus.be.server.domain.product.dto;

public record ProductUpdateDto(
        String name,
        Double price,
        long stockAdjustment
) {}