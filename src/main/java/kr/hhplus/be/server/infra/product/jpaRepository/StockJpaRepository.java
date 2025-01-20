package kr.hhplus.be.server.infra.product.jpaRepository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.product.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProductId(Long productId);

    List<Stock> findByProductIdIn(List<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.productId = :productId")
    Optional<Stock> findByProductIdWithLock(@Param("productId") Long productId);
}
