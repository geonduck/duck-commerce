package kr.hhplus.be.server.infra.product.jpaRepository;

import kr.hhplus.be.server.domain.product.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProductId(Long productId);

    List<Stock> findByProductIdIn(List<Long> productIds);
}
