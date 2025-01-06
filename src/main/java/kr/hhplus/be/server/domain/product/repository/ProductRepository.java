package kr.hhplus.be.server.domain.product.repository;

import aj.org.objectweb.asm.commons.Remapper;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;
import kr.hhplus.be.server.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product findById(long id);

    int getActualStock(long id);

    Page<Product> findAll(Pageable pageRequest);

}
