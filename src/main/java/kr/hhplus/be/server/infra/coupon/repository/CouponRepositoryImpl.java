package kr.hhplus.be.server.infra.coupon.repository;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.infra.coupon.jpaRepository.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository jpaRepository;

    @Override
    public Optional<Coupon> findById(long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<Coupon> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return jpaRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findByIdWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id);
    }

    @Override
    public List<Coupon> findAll() {
        return jpaRepository.findAll();
    }
}
