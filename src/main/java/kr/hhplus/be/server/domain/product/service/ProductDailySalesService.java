package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.entity.ProductDailySales;
import kr.hhplus.be.server.domain.product.repository.ProductDailySalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDailySalesService {

    private final ProductDailySalesRepository productDailySalesRepository;

    /**
     * 일일 판매량 업데이트
     */
    @Transactional
    public void updateDailySales(Long productId, int quantitySold) {
        LocalDate today = LocalDate.now();

        // 오늘의 판매량 데이터를 조회 또는 생성
        ProductDailySales dailySales = productDailySalesRepository.findByProductIdAndDate(productId, today)
                .orElseGet(() -> ProductDailySales.builder()
                        .productId(productId)
                        .date(today)
                        .dailyQuantitySold(0)
                        .lastUpdated(LocalDate.now().atStartOfDay())
                        .build());

        // 판매량 업데이트
        dailySales.setDailyQuantitySold(dailySales.getDailyQuantitySold() + quantitySold);
        dailySales.setLastUpdated(LocalDate.now().atStartOfDay());

        // 저장
        productDailySalesRepository.save(dailySales);
    }

    public List<ProductDailySales> getTopSellingProductsForLast3Days() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        return productDailySalesRepository.findTopSellingProductsForPeriod(threeDaysAgo, today, PageRequest.of(0, 5));
    }

}