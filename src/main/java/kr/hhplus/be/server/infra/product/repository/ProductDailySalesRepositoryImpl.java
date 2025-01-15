package kr.hhplus.be.server.infra.product.repository;

import kr.hhplus.be.server.domain.product.entity.ProductDailySales;
import kr.hhplus.be.server.domain.product.repository.ProductDailySalesRepository;
import kr.hhplus.be.server.infra.product.jpaRepository.ProductDailySalesJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductDailySalesRepositoryImpl implements ProductDailySalesRepository {
    public final ProductDailySalesJpaRepository jpaRepository;

    @Override
    public Optional<ProductDailySales> findByProductIdAndDate(Long productId, LocalDate today) {
        return jpaRepository.findByProductIdAndDate(productId, today);
    }

    @Override
    public void save(ProductDailySales dailySales) {
        jpaRepository.save(dailySales);
    }

    @Override
    public List<ProductDailySales> findTopSellingProductsForPeriod(LocalDate threeDaysAgo, LocalDate today, PageRequest of) {
        return jpaRepository.findTopSellingProductsForPeriod(threeDaysAgo, today, of);
    }
}
