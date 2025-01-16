package kr.hhplus.be.server.facade;

import kr.hhplus.be.server.config.exception.CommonException;
import kr.hhplus.be.server.config.exception.ErrorCode;
import lombok.Getter;

@Getter
public class FacadeException extends CommonException {

    private final ErrorCode errorCode;

    public FacadeException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
