package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.coupon.CouponErrorCode;
import kr.hhplus.be.server.domain.coupon.CouponStatus;
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
            throw new DomainException(CouponErrorCode.ALREADY_EXPIRED_EXCEPTION);
        }

        long issuedCount = couponAssignmentRepository.countByCouponId(id);
        if (issuedCount >= coupon.getMaxIssuance()) {
            throw new DomainException(CouponErrorCode.MAX_COUPON_EXCEPTION);
        }

        boolean alreadyAssigned = couponAssignmentRepository.existsByCouponIdAndUserId(id, userId);
        if (alreadyAssigned) {
            throw new DomainException(CouponErrorCode.ALREADY_ISSUE_EXCEPTION);
        }

        CouponAssignment assignment = CouponAssignment.builder()
                .userId(userId)
                .coupon(coupon)
                .status(CouponStatus.ISSUED)
                .build();

        return CouponAssignmentDto.of(couponAssignmentRepository.save(assignment));
    }

    private Coupon findCouponById(Long id) {
        return couponRepository.findById(id).orElseThrow(() -> new DomainException(CouponErrorCode.NOT_FIND_EXCEPTION));
    }

    @Transactional
    public CouponAssignmentDto useCoupon(Long assignmentId, String userId) {
        CouponAssignment assignment = couponAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new DomainException(CouponErrorCode.NON_EXISTENT_ISSUE_EXCEPTION));

        if (!assignment.getUserId().equals(userId)) {
            throw new DomainException(CouponErrorCode.NOT_USER_COUPON_EXCEPTION);
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

    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }
}
