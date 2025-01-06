package kr.hhplus.be.server.domain.product;


import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDomainDto getProduct(Long productId) {
        Product product = productRepository.findById(productId);

        ProductDomainDto productDto = ProductDomainDto.form(product);
        productDto.validateStock();

        return productDto;
    }

    public void verifyProductStock(Long productId) {
        Product product = productRepository.findById(productId);

        ProductDomainDto productDto = ProductDomainDto.form(product);
        int actualStock = productRepository.getActualStock(productId);

        productDto.validateStockMatch(actualStock);
    }


    public Page<ProductDomainDto> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductDomainDto::form);
    }
}
