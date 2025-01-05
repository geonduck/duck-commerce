package kr.hhplus.be.server.infra.balance.jpaRepository;

import kr.hhplus.be.server.domain.balance.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceJpaRepository extends JpaRepository<Balance, Long> {
    Balance findByUserId(String userId);
}
