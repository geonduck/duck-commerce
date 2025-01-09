package kr.hhplus.be.server.infra.coupon.jpaRepository;

import kr.hhplus.be.server.domain.coupon.CouponStatus;
import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponAssignmentJpaRepository extends JpaRepository<CouponAssignment, Long> {
    int countByCouponId(long id);

    boolean existsByCouponIdAndUserId(long id, String userId);

    List<CouponAssignment> findByUserId(String userId);

    Page<CouponAssignment> findByUserId(String userId, Pageable pageable);

    Page<CouponAssignment> findByUserIdAndStatusAndCoupon_ExpiredAtAfter(String userId, CouponStatus couponStatus, LocalDateTime now, Pageable pageable);
}
