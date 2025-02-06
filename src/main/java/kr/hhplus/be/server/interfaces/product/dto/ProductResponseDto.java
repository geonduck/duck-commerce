package kr.hhplus.be.server.interfaces.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    @JsonProperty("name") String name;
    @JsonProperty("price") double price;
    @JsonProperty("stock") int stock;
}
