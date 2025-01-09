package kr.hhplus.be.server.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDailySales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId; // 상품 ID

    private LocalDate date; // 판매 날짜

    private int dailyQuantitySold; // 해당 날짜 판매량

    private LocalDateTime lastUpdated; // 마지막 업데이트 시간

    public void incrementSales(int quantity) {
        this.dailyQuantitySold += quantity;
        this.lastUpdated = LocalDateTime.now();
    }
}