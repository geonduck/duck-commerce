package kr.hhplus.be.server.facade.balance;

import kr.hhplus.be.server.config.redis.RedisLock;
import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceRequestDto;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BalanceFacade {

    private final BalanceService balanceService;

    public BalanceResponseDto getBalance(String userId) {
        BalanceDomainDto domainDto = balanceService.getBalance(new BalanceDomainDto(userId, null));
        return new BalanceResponseDto(domainDto.userId(), domainDto.amount(), LocalDateTime.now());
    }

    @RedisLock(key = "#requestDto.userId()", expiration = 60, keyPrefix = "balance")
    public BalanceResponseDto chargeBalance(BalanceRequestDto requestDto) {
        BalanceDomainDto domainDto = balanceService.charge(
                new BalanceDomainDto(requestDto.userId(), (double) requestDto.amount())
        );
        return new BalanceResponseDto(domainDto.userId(), domainDto.amount(), LocalDateTime.now());
    }

    @RedisLock(key = "#requestDto.userId()", expiration = 60, keyPrefix = "balance")
    public BalanceResponseDto useBalance(BalanceRequestDto requestDto) {
        BalanceDomainDto domainDto = balanceService.use(
                new BalanceDomainDto(requestDto.userId(), (double) requestDto.amount())
        );
        return new BalanceResponseDto(domainDto.userId(), domainDto.amount(), LocalDateTime.now());
    }

    public BalanceResponseDto cancelUseBalance(BalanceRequestDto requestDto) {
        BalanceDomainDto domainDto = balanceService.cancelUse(
                new BalanceDomainDto(requestDto.userId(), (double) requestDto.amount())
        );
        return new BalanceResponseDto(domainDto.userId(), domainDto.amount(), LocalDateTime.now());
    }

}