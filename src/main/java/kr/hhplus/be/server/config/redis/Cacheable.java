package kr.hhplus.be.server.config.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
    String key();
    long ttl() default 1; // 기본 TTL은 1일
    TimeUnit timeUnit() default TimeUnit.DAYS;
}