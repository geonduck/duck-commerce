package kr.hhplus.be.server.domain.product.service;


import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.product.ProductErrorCode;
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
        Product product = findProductById(productId);
        StockDto stock = stockService.getStock(productId);
        return ProductDomainDto.of(product, stock);
    }

    @Transactional
    public void updateProduct(ProductUpdateDto updateDto) {
        Product product = findProductById(updateDto.productId());
        updateDto.validateUpdate();
        stockService.adjust(updateDto);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new DomainException(ProductErrorCode.NOT_FIND_EXCEPTION));
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
