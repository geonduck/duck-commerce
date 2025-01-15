package kr.hhplus.be.server.domain.coupon.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseTimeEntity;
import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.coupon.CouponErrorCode;
import kr.hhplus.be.server.domain.coupon.CouponStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class CouponAssignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;  // 사용자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon; // 쿠폰 ID와 연관 관계

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status; // 상태 (발급, 사용됨, 만료됨)

    public void use() {
        validateCanUse();
        this.status = CouponStatus.USED;
    }

    public void expire() {
        if (this.status != CouponStatus.USED) {
            this.status = CouponStatus.EXPIRED;
        }
    }

    private void validateCanUse() {
        if (this.status != CouponStatus.ISSUED) {
            throw new DomainException(CouponErrorCode.NOT_USED_COUPON_EXCEPTION);
        }

        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            expire();
            throw new DomainException(CouponErrorCode.ALREADY_EXPIRED_EXCEPTION);
        }
    }

    public boolean isExpired() {
        return this.status == CouponStatus.EXPIRED;
    }

    public boolean isUsed() {
        return this.status == CouponStatus.USED;
    }
}
