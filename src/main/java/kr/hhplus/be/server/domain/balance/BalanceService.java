package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository;
import kr.hhplus.be.server.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceService {


    private final BalanceRepository balanceRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    private Balance findBalanceByUserId(String userId) {
        return balanceRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    @Transactional
    public BalanceDomainDto getBalance(BalanceDomainDto dto) {
        return BalanceDomainDto.from(findBalanceByUserId(dto.userId()));
    }

    @Transactional
    public BalanceDomainDto charge(BalanceDomainDto dto) {
        BalanceStatus status = BalanceStatus.CHARGE;
        try {
            dto.validateCharge();

            Balance balance = findBalanceByUserId(dto.userId());
            balance.charge(dto.amount());

            Balance savedBalance = balanceRepository.save(balance);
            return BalanceDomainDto.from(savedBalance);
        } catch (Exception e) {
            status = BalanceStatus.FAIL;
            throw new IllegalStateException(e.getMessage());
        } finally {
            insertHistory(dto, status);
        }
    }

    @Transactional
    public BalanceDomainDto use(BalanceDomainDto dto) {
        BalanceStatus status = BalanceStatus.USE;
        try {
            dto.validateUse();

            Balance balance = findBalanceByUserId(dto.userId());
            balance.use(dto.amount());

            Balance savedBalance = balanceRepository.save(balance);
            return BalanceDomainDto.from(savedBalance);
        } catch (Exception e) {
            status = BalanceStatus.FAIL;
            throw new IllegalStateException(e.getMessage());
        } finally {
            insertHistory(dto, status);
        }

    }

    @Transactional
    public BalanceDomainDto cancelUse(BalanceDomainDto dto) {
        BalanceStatus status = BalanceStatus.CANCEL;
        try {
            Balance balance = findBalanceByUserId(dto.userId());
            balance.cancelUse(dto.amount());

            Balance savedBalance = balanceRepository.save(balance);
            return BalanceDomainDto.from(savedBalance);
        } catch (Exception e) {
            status = BalanceStatus.FAIL;
            throw new IllegalStateException(e.getMessage());
        } finally {
            insertHistory(dto, status);
        }


    }

    private void insertHistory(BalanceDomainDto dto, BalanceStatus status) {
        balanceHistoryRepository.save(BalanceHistory.builder().userId(dto.userId()).amountChanged(dto.amount()).status(status).build());
    }

}
