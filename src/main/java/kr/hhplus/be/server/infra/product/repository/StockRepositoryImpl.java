package kr.hhplus.be.server.infra.product.repository;

import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.repository.StockRepository;
import kr.hhplus.be.server.infra.product.jpaRepository.StockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    public final StockJpaRepository jpaRepository;

    @Override
    public Optional<Stock> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<Stock> findByProductIdIn(List<Long> productIds) {
        return jpaRepository.findByProductIdIn(productIds);
    }

    @Override
    public Stock save(Stock stock) {
        return jpaRepository.save(stock);
    }

}
