package kr.hhplus.be.server.infra.balance.repository;

import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.infra.balance.jpaRepository.BalanceHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BalanceHistoryRepositoryImpl implements BalanceHistoryRepository {

    private BalanceHistoryJpaRepository jpaRepository;

    @Override
    public BalanceHistory save(BalanceHistory balanceHistory) {
        return jpaRepository.save(balanceHistory);
    }
}
