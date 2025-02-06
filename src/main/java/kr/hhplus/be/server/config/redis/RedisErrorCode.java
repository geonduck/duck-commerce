package kr.hhplus.be.server.config.redis;

import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RedisErrorCode implements ErrorCode {
    REDIS_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Redisson 클라이언트를 생성 실패"),
    REDIS_LOCK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 잠금 획득 실패");

    private final HttpStatus httpStatus;
    private final String message;

}
