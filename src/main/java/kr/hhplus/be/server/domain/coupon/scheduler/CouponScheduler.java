package kr.hhplus.be.server.domain.coupon.scheduler;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final CouponService couponService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String COUPON_QUEUE_KEY = "coupon_queue:";

    @Scheduled(fixedDelay = 1000)
    public void processCouponQueue() {
        Set<String> couponIds = redisTemplate.keys(COUPON_QUEUE_KEY + "*");
        if (couponIds != null) {
            for (String couponQueueKey : couponIds) {
                Set<TypedTuple<Object>> users = redisTemplate.opsForZSet().popMin(couponQueueKey, 1);
                if (users != null && !users.isEmpty()) {
                    TypedTuple<Object> user = users.iterator().next();
                    String userCoupon = (String) user.getValue();
                    String[] parts = userCoupon.split(":");
                    String userId = parts[0];
                    Long couponId = Long.parseLong(parts[1]);

                    try {
                        couponService.assignCoupon(couponId, userId);
                    } catch (DomainException e) {
                        // 쿠폰 발급 실패 시 로그 출력
                        log.error("쿠폰 발급 실패: " + e.getMessage());
                    } finally {
                        redisTemplate.opsForZSet().remove(couponQueueKey, userCoupon);
                    }
                }
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanUpOldRequests() {
        Set<String> couponIds = redisTemplate.keys(COUPON_QUEUE_KEY + "*");
        if (couponIds != null) {
            long expirationTime = System.currentTimeMillis() - (5 * 60 * 1000); // 5분 전 기준
            for (String couponQueueKey : couponIds) {
                redisTemplate.opsForZSet().removeRangeByScore(couponQueueKey, 0, expirationTime);
            }
        }
    }

    @Scheduled(fixedDelay = 300000) // 5분마다 동기화
    public void syncIssuedCount() {
        Set<String> couponIds = redisTemplate.keys(COUPON_QUEUE_KEY + "*");
        if (couponIds != null) {
            for (String couponQueueKey : couponIds) {
                Long couponId = Long.parseLong(couponQueueKey.replace(COUPON_QUEUE_KEY, ""));
                couponService.syncIssuedCountWithDB(couponId);
            }
        }
    }

}
