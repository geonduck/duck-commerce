package kr.hhplus.be.server.config.redis;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(cacheable)")
    public Object cache(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String key = cacheable.key();
        long ttl = cacheable.ttl();
        TimeUnit timeUnit = cacheable.timeUnit();

        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            return cachedValue;
        }

        Object result = joinPoint.proceed();
        redisTemplate.opsForValue().set(key, result, ttl, timeUnit);
        return result;
    }
}