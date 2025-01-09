package kr.hhplus.be.server.domain.product;


import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.dto.ProductListDto;
import kr.hhplus.be.server.domain.product.dto.ProductUpdateDto;
import kr.hhplus.be.server.domain.product.dto.StockDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final StockService stockService;

    public ProductDomainDto getProduct(Long productId) {
        // 1. 상품 정보 조회
        Product product = findProductById(productId);

        // 2. 재고 정보 조회
        StockDto stock = stockService.getStock(productId);

        return ProductDomainDto.of(product, stock);
    }

    @Transactional
    public void updateProduct(Long productId, int amonut) {
        Product product = findProductById(productId);
        // 재고 수정이 필요한 경우 StockService를 통해 처리
        if (amonut != 0) {
            if (amonut > 0) {
                stockService.increase(productId, amonut);
            } else {
                stockService.decrease(productId, amonut);
            }
        }
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    public Page<ProductListDto> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        // 상품 ID 리스트로 재고 정보 일괄 조회
        List<Long> productIds = products.getContent().stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        Map<Long, StockDto> stockMap = stockService.getStocksByProductIds(productIds);

        return products.map(product ->
                ProductListDto.of(product, stockMap.get(product.getId()))
        );
    }
}
