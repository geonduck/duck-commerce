package kr.hhplus.be.server.infra.balance.repository;

import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository;
import kr.hhplus.be.server.infra.balance.jpaRepository.BalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BalanceRepositoryImpl implements BalanceRepository {

    private final BalanceJpaRepository jpaRepository;

    @Override
    public Optional<Balance> findByUserIdWithLock(String userId) {
        return jpaRepository.findByUserIdWithLock(userId);
    }

    @Override
    public Optional<Balance> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Balance save(Balance balance) {
        return jpaRepository.save(balance);
    }
}
