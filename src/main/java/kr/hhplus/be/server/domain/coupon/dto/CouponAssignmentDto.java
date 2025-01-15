package kr.hhplus.be.server.domain.coupon.dto;

import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;

public record CouponAssignmentDto(Long id, String userId, String status, CouponDto coupon) {
    public static CouponAssignmentDto of(CouponAssignment assignment) {
        return new CouponAssignmentDto(assignment.getId(), assignment.getUserId(), assignment.getStatus().name(), CouponDto.of(assignment.getCoupon()));
    }
}