package kr.hhplus.be.server.config.redis;

import kr.hhplus.be.server.config.exception.CommonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        String keyPrefix = redisLock.keyPrefix();
        String dynamicKey = generateDynamicKey(redisLock.key());
        String key = keyPrefix + ":" + dynamicKey; // 락 키에 유니크 컨텍스트 추가
        int expiration = redisLock.expiration();

        RLock lock = redissonClient.getLock(key);

        log.info("Attempting to acquire lock: {}", key);

        // 대기 시간을 늘리고 만료 시간을 조정 가능
        boolean isLocked = lock.tryLock(30, expiration, TimeUnit.SECONDS);
        if (!isLocked) {
            log.error("Lock acquisition failed: {}", key);
            throw new CommonException(RedisErrorCode.REDIS_LOCK_FAILED);
        }
        try {
            return joinPoint.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) { // 안전한 unlock 처리
                lock.unlock();
                log.info("Lock released successfully: {}", key);
            }
        }

    }

    private String generateDynamicKey(String baseKey) {
        // Unique Key 생성 (예: 스레드 ID, 타임스탬프 포함)
        return baseKey + ":" + Thread.currentThread().getId() + ":" + System.currentTimeMillis();
    }
}
