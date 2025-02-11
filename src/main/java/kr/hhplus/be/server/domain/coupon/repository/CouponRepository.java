package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findById(long id);

    Page<Coupon> findAll(Pageable pageable);

    Coupon save(Coupon coupon);

    Optional<Coupon> findByIdWithLock(Long id);

    List<Coupon> findAll();
}
