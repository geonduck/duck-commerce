package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.CouponStatus;
import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponAssignmentRepository {
    int countByCouponId(long id);

    CouponAssignment save(CouponAssignment couponAssignment);

    boolean existsByCouponIdAndUserId(long id, String userId);

    List<CouponAssignment> findByUserId(String userId);

    Optional<CouponAssignment> findById(Long assignmentId);

    Page<CouponAssignment> findByUserId(String userId, Pageable pageable);
}
