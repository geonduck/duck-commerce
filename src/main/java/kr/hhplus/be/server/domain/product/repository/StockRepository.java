package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.entity.Stock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockRepository {

    Optional<Stock> findByProductId(Long productId);
    List<Stock> findByProductIdIn(List<Long> productIds);
}