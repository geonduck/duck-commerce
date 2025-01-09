package kr.hhplus.be.server.infra.coupon.jpaRepository;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
