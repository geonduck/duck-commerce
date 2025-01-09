package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.dto.StockDto;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    @Transactional
    public void decrease(Long productId, long quantity) {
        Stock stock = findStockByProductId(productId);
        stock.decrease(quantity);
    }

    @Transactional
    public void increase(Long productId, long quantity) {
        Stock stock = findStockByProductId(productId);
        stock.increase(quantity);
    }

    @Transactional
    public void cancel(Long productId, long quantity) {
        Stock stock = findStockByProductId(productId);
        stock.increase(quantity);
    }

    private Stock findStockByProductId(Long productId) {
        return stockRepository.findByProductId(productId).orElseThrow(() -> new IllegalArgumentException("상품의 재고 정보를 찾을 수 없습니다"));
    }

    // 재고 조회
    public StockDto getStock(Long productId) {
        Stock stock = findStockByProductId(productId);
        return StockDto.from(stock);
    }

    public Map<Long, StockDto> getStocksByProductIds(List<Long> productIds) {
        return stockRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        Stock::getProductId,
                        StockDto::from
                ));
    }
}