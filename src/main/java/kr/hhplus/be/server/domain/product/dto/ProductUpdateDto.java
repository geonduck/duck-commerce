package kr.hhplus.be.server.domain.product.dto;

public record ProductUpdateDto(
        Long productId, int amount
) {
    public void validateUpdate() {
        if (amount() == 0) {
            throw new IllegalStateException("변경할 수량이 없습니다");
        }
    }
}