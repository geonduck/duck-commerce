package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private static final long MAX_BALANCE = 10_000_000;

    private final BalanceRepository balanceRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    @Transactional
    public BalanceDomainDto charge(BalanceDomainDto dto) {
        dto.validate();

        Balance balance = balanceRepository.findByUserId(dto.userId());
        if (balance == null) {
            balance = Balance.builder()
                    .userId(dto.userId())
                    .amount(0)
                    .build();
        }

        if (balance.getAmount() + dto.amount() > MAX_BALANCE) {
            throw new IllegalStateException("최대 보유 가능 금액은 10,000,000원 입니다");
        }

        balance.charge(dto.amount());
        Balance savedBalance = balanceRepository.save(balance);

        balanceHistoryRepository.save(BalanceHistory.builder()
                .userId(dto.userId())
                .amountChanged(dto.amount())
                .status(BalanceStaut.CHARGE)
                .build()
        );

        return BalanceDomainDto.from(savedBalance);
    }
}
