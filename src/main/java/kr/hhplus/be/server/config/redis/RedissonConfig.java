package kr.hhplus.be.server.config.redis;

import kr.hhplus.be.server.config.exception.CommonException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private static final Logger log = LoggerFactory.getLogger(RedissonConfig.class);

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        try {
            return Redisson.create(config);
        } catch (Exception e) {
            log.error("Failed to create Redisson client", e);
            throw new CommonException(RedisErrorCode.REDIS_CONNECTION_FAILED);
        }
    }
}