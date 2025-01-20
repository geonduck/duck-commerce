package kr.hhplus.be.server.infra.product.jpaRepository;

import kr.hhplus.be.server.domain.product.entity.ProductDailySales;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductDailySalesJpaRepository extends JpaRepository<ProductDailySales, Long> {
    Optional<ProductDailySales> findByProductIdAndDate(Long productId, LocalDate date);

    @Query("SELECT p FROM ProductDailySales p " +
            "WHERE p.date BETWEEN :startDate AND :endDate " +
            "ORDER BY p.dailyQuantitySold DESC")
    List<ProductDailySales> findTopSellingProductsForPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
