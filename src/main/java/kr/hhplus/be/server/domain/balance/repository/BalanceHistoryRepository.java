package kr.hhplus.be.server.domain.balance.repository;

import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;

public interface BalanceHistoryRepository {
    BalanceHistory save(BalanceHistory balanceHistory);
}
