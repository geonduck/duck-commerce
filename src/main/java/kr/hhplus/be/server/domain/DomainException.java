package kr.hhplus.be.server.domain;

import kr.hhplus.be.server.config.exception.CommonException;
import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DomainException extends CommonException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
