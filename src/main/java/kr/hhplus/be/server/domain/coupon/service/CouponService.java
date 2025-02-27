package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.config.redis.Cacheable;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponAssignmentRepository couponAssignmentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String COUPON_QUEUE_KEY_PREFIX = "coupon_queue:";
    private static final String COUPON_ASSIGNMENT_KEY_PREFIX = "coupon_assignment:";

    @Transactional
    public CouponAssignmentDto assignCoupon(long id, String userId) {
        log.info("assignCoupon start");
        Coupon coupon = validateCoupon(id);

        long issuedCount = getIssuedCount(id);

        if (issuedCount >= coupon.getMaxIssuance()) {
            throw new DomainException(CouponErrorCode.MAX_COUPON_EXCEPTION);
        }

        String redisKey = userId + ":" + id;
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(COUPON_ASSIGNMENT_KEY_PREFIX + id, redisKey))) {
            throw new DomainException(CouponErrorCode.ALREADY_ISSUE_EXCEPTION);
        }

        CouponAssignment assignment = createCouponAssignment(coupon, userId);
        CouponAssignmentDto result = CouponAssignmentDto.of(couponAssignmentRepository.save(assignment));
        redisTemplate.opsForSet().add(COUPON_ASSIGNMENT_KEY_PREFIX + id, redisKey);
        return result;
    }

    private Coupon validateCoupon(long id) {
        Coupon coupon = findCouponById(id);
        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new DomainException(CouponErrorCode.ALREADY_EXPIRED_EXCEPTION);
        }
        return coupon;
    }

    private long getIssuedCount(long id) {
        Long issuedCount = redisTemplate.opsForSet().size(COUPON_ASSIGNMENT_KEY_PREFIX + id);
        if (issuedCount == null) {
            issuedCount = countByCouponId(id);
        }
        return issuedCount;
    }

    private CouponAssignment createCouponAssignment(Coupon coupon, String userId) {
        return CouponAssignment.builder()
                .userId(userId)
                .coupon(coupon)
                .status(CouponStatus.ISSUED)
                .build();
    }

    public boolean requestCoupon(Long couponId, String userId) {
        long issuedCount = getIssuedCount(couponId);
        int maxIssuance = getMaxIssuance(couponId);

        if (issuedCount >= maxIssuance) return false;

        double timestamp = (double) System.currentTimeMillis(); // 요청 시간 기반 우선순위
        redisTemplate.opsForZSet().add(COUPON_QUEUE_KEY_PREFIX + couponId, userId + ":" + couponId, timestamp);
        log.info("쿠폰 발급 요청: userId=" + userId + ", couponId=" + couponId + ", timestamp=" + timestamp);
        return true;
    }

    private int getMaxIssuance(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        return coupon.getMaxIssuance();
    }

    public long countByCouponId(long id) {
        return couponAssignmentRepository.countByCouponId(id);
    }

    @Cacheable(key = "#id", ttl = 60)
    private Coupon findCouponById(Long id) {
        return couponRepository.findByIdWithLock(id).orElseThrow(() -> new DomainException(CouponErrorCode.NOT_FIND_EXCEPTION));
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


    public void syncIssuedCountWithDB(long id) {
        Long issuedCount = redisTemplate.opsForSet().size(COUPON_ASSIGNMENT_KEY_PREFIX + id);
        long dbIssuedCount = countByCouponId(id);

        if (issuedCount == null || issuedCount != dbIssuedCount) {
            List<CouponAssignment> assignments = couponAssignmentRepository.findByCouponId(id);
            Set<String> dbKeys = assignments.stream()
                    .map(assignment -> assignment.getUserId() + ":" + id)
                    .collect(Collectors.toSet());

            Set<Object> redisKeys = redisTemplate.opsForSet().members(COUPON_ASSIGNMENT_KEY_PREFIX + id);

            // Redis에 없는 데이터를 추가
            for (String dbKey : dbKeys) {
                if (!redisKeys.contains(dbKey)) {
                    redisTemplate.opsForSet().add(COUPON_ASSIGNMENT_KEY_PREFIX + id, dbKey);
                }
            }

            // Redis에 있지만 DB에 없는 데이터를 제거
            for (Object redisKey : redisKeys) {
                if (!dbKeys.contains(redisKey)) {
                    redisTemplate.opsForSet().remove(COUPON_ASSIGNMENT_KEY_PREFIX + id, redisKey);
                }
            }

        }
    }
}
