package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.dto.CouponDto;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;
import kr.hhplus.be.server.domain.coupon.repository.CouponAssignmentRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponAssignmentRepository couponAssignmentRepository;

    @Transactional
    public CouponAssignmentDto assignCoupon(long id, String userId) {
        Coupon coupon = findCouponById(id);

        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("이미 만료된 쿠폰입니다.");
        }

        long issuedCount = couponAssignmentRepository.countByCouponId(id);
        if (issuedCount >= coupon.getMaxIssuance()) {
            throw new IllegalStateException("쿠폰 발급 수량이 초과되었습니다.");
        }

        boolean alreadyAssigned = couponAssignmentRepository.existsByCouponIdAndUserId(id, userId);
        if (alreadyAssigned) {
            throw new IllegalStateException("이미 해당 쿠폰을 발급받았습니다.");
        }

        CouponAssignment assignment = CouponAssignment.builder()
                .userId(userId)
                .coupon(coupon)
                .status(CouponStatus.ISSUED)
                .build();

        return CouponAssignmentDto.of(couponAssignmentRepository.save(assignment));
    }

    private Coupon findCouponById(Long id) {
        return couponRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
    }

    @Transactional
    public CouponAssignmentDto useCoupon(Long assignmentId, String userId) {
        CouponAssignment assignment = couponAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰 발급 내역입니다."));

        if (!assignment.getUserId().equals(userId)) {
            throw new IllegalStateException("해당 사용자의 쿠폰이 아닙니다.");
        }

        assignment.use();

        return CouponAssignmentDto.of(couponAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public Page<CouponDto> getAllCoupons(Pageable pageable) {
        Page<Coupon> coupons = couponRepository.findAll(pageable);
        return coupons.map(CouponDto::of);
    }

    @Transactional(readOnly = true)
    public Page<CouponAssignmentDto> getUserCoupons(String userId, Pageable pageable) {
        Page<CouponAssignment> assignments = couponAssignmentRepository.findByUserId(userId, pageable);
        return assignments.map(CouponAssignmentDto::of);
    }

}
