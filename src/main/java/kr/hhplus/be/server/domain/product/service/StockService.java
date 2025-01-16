package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.product.ProductErrorCode;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
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
@Transactional
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    @Transactional
    public void decrease(Stock stock, int quantity) {
        stock.decrease(quantity);
    }

    @Transactional
    public void increase(Stock stock, int quantity) {
        stock.increase(quantity);
    }

    private Stock findStockByProductId(Long productId) {
        return stockRepository.findByProductIdWithLock(productId).orElseThrow(() -> new DomainException(ProductErrorCode.NOT_FIND_STOCK_EXCEPTION));
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

    @Transactional
    public StockDto adjust(ProductUpdateDto updateDto) {
        Stock stock = findStockByProductId(updateDto.productId());

        if (updateDto.amount() > 0) {
            increase(stock, updateDto.amount());
        } else {
            decrease(stock, updateDto.amount());
        }
        return StockDto.from(save(stock));
    }

    public Stock save(Stock stock) {
        return stockRepository.save(stock);
    }
}