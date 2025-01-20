package kr.hhplus.be.server.facade.coupon;

import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponRequestDto;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CouponFacade {

    private final CouponService couponService;

    public CouponResponseDto assignCoupon(CouponRequestDto requestDto) {
        CouponAssignmentDto assignmentDto = couponService.assignCoupon(requestDto.couponId(), requestDto.userId());
        return new CouponResponseDto(
                assignmentDto.coupon().id(),
                assignmentDto.coupon().name(),
                assignmentDto.coupon().expiredAt()
        );
    }

    public List<CouponResponseDto> getUserCoupons(String userId, Pageable pageable) {
        Page<CouponAssignmentDto> userCoupons = couponService.getUserCoupons(userId, pageable);
        return userCoupons.stream()
                .map(assignment -> new CouponResponseDto(
                        assignment.coupon().id(),
                        assignment.coupon().name(),
                        assignment.coupon().expiredAt()
                ))
                .collect(Collectors.toList());
    }

    public List<CouponResponseDto> getAvailableCoupons(Pageable pageable) {
        Page<CouponResponseDto> availableCoupons = couponService.getAllCoupons(pageable)
                .map(coupon -> new CouponResponseDto(
                        coupon.id(),
                        coupon.name(),
                        coupon.expiredAt()
                ));
        return availableCoupons.getContent();
    }
}