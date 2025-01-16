package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.entity.ProductDailySales;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductDailySalesRepository {
    Optional<ProductDailySales> findByProductIdAndDate(Long productId, LocalDate today);

    void save(ProductDailySales dailySales);

    List<ProductDailySales> findTopSellingProductsForPeriod(LocalDate threeDaysAgo, LocalDate today, PageRequest of);
}
