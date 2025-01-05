package kr.hhplus.be.server.infra.balance.repository;

import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository;
import kr.hhplus.be.server.infra.balance.jpaRepository.BalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BalanceRepositoryImpl implements BalanceRepository {

    private final BalanceJpaRepository jpaRepository;

    @Override
    public Balance findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Balance save(Balance balance) {
        return jpaRepository.save(balance);
    }
}
