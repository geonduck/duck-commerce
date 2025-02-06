package kr.hhplus.be.server.domain.product.scheduler;

import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductScheduler {

    private final ProductDailySalesService productDailySalesService;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void refreshCache() {
        productDailySalesService.getTopSellingProductsForLast3Days();
    }

}
