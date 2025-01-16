package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface ProductRepository {
    Optional<Product> findById(long id);

    Page<Product> findAll(Pageable pageRequest);

    Product save(Product product);
}
