package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.scheduler.CouponScheduler;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouponSchedulerUnitTest {

    @InjectMocks
    private CouponScheduler couponScheduler;

    @Mock
    private CouponService couponService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    private static final String COUPON_QUEUE_KEY_PREFIX = "coupon_queue:";
    private static final long TEST_COUPON_ID = 1L;
    private static final String TEST_USER_ID = "testUser";

    @Test
    @DisplayName("쿠폰 큐 처리 테스트")
    void processCouponQueue() {
        // given
        String couponQueueKey = COUPON_QUEUE_KEY_PREFIX + TEST_COUPON_ID;
        when(redisTemplate.keys(COUPON_QUEUE_KEY_PREFIX + "*")).thenReturn(Set.of(couponQueueKey));
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.popMin(couponQueueKey, 1)).thenReturn(Set.of(new ZSetOperations.TypedTuple<Object>() {
            @Override
            public Object getValue() {
                return TEST_USER_ID + ":" + TEST_COUPON_ID;
            }

            @Override
            public Double getScore() {
                return (double) System.currentTimeMillis();
            }

            @Override
            public int compareTo(ZSetOperations.TypedTuple<Object> o) {
                return 0;
            }
        }));

        // when
        couponScheduler.processCouponQueue();

        // then
        verify(couponService).assignCoupon(TEST_COUPON_ID, TEST_USER_ID);
        verify(zSetOperations).remove(couponQueueKey, TEST_USER_ID + ":" + TEST_COUPON_ID);
    }

    @Test
    @DisplayName("쿠폰 동기화 테스트")
    void syncIssuedCount() {
        // given
        String couponQueueKey = COUPON_QUEUE_KEY_PREFIX + TEST_COUPON_ID;
        when(redisTemplate.keys(COUPON_QUEUE_KEY_PREFIX + "*")).thenReturn(Set.of(couponQueueKey));

        // when
        couponScheduler.syncIssuedCount();

        // then
        verify(couponService).syncIssuedCountWithDB(TEST_COUPON_ID);
    }
}