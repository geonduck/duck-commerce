package kr.hhplus.be.server.domain.balance.repository;

import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;

import java.util.List;

public interface BalanceHistoryRepository {
    BalanceHistory save(BalanceHistory balanceHistory);

    List<BalanceHistory> findAllByUserId(String userId);
}
