package kr.hhplus.be.server.domain.balance.service;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.balance.BalanceErrorCode;
import kr.hhplus.be.server.domain.balance.BalanceStatus;
import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    @Transactional
    protected Balance findBalanceByUserId(String userId) {
        return balanceRepository.findByUserIdWithLock(userId).orElseThrow(() -> new DomainException(BalanceErrorCode.NOT_FIND_EXCEPTION));
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
            Balance savedBalance = save(balance);
            return BalanceDomainDto.from(savedBalance);
        } catch (DomainException e) {
            status = BalanceStatus.FAIL;
            throw new DomainException(e.getErrorCode());
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

            Balance savedBalance = save(balance);
            return BalanceDomainDto.from(savedBalance);
        } catch (DomainException e) {
            status = BalanceStatus.FAIL;
            throw new DomainException(e.getErrorCode());
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

            Balance savedBalance = save(balance);
            return BalanceDomainDto.from(savedBalance);
        } catch (DomainException e) {
            status = BalanceStatus.FAIL;
            throw new DomainException(e.getErrorCode());
        } finally {
            insertHistory(dto, status);
        }


    }

    @Transactional
    protected void insertHistory(BalanceDomainDto dto, BalanceStatus status) {
        balanceHistoryRepository.save(BalanceHistory.builder().userId(dto.userId()).amountChanged(dto.amount()).status(status).build());
    }

    @Transactional
    public Balance save(Balance balance) {
        return balanceRepository.save(balance);
    }

    public List<BalanceHistory> findAllByUserId(String userId) {
        return balanceHistoryRepository.findAllByUserId(userId);
    }
}
