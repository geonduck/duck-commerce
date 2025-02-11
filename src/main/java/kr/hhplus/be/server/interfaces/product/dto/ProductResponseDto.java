package kr.hhplus.be.server.interfaces.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductResponseDto(
        @JsonProperty("name") String name,
        @JsonProperty("price") double price,
        @JsonProperty("stock") int stock
) {
}
