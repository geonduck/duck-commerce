package kr.hhplus.be.server.domain.balance.repository;

import kr.hhplus.be.server.domain.balance.entity.Balance;

import java.util.Optional;

public interface BalanceRepository {
    Optional<Balance> findByUserIdWithLock(String userId);

    Optional<Balance> findByUserId(String userId);

    Balance save(Balance balance);
}
