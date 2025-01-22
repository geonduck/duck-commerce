package kr.hhplus.be.server.domain.balance.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseTimeEntity;
import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.balance.BalanceErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Balance extends BaseTimeEntity {

    private static final long MAX_BALANCE = 10_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;  // 사용자 ID
    private double amount;  // 잔액

    public void charge(double amount) {
        if (this.amount + amount > MAX_BALANCE) {
            throw new DomainException(BalanceErrorCode.MAX_BALANCE_EXCEPTION);
        }
        this.amount += amount;
    }

    public void use(double amount) {
        if (this.amount - amount < 0) {
            throw new DomainException(BalanceErrorCode.HAVE_BALANCE_EXCEPTION);
        }
        this.amount -= amount;
    }

    public void cancelUse(double amount) {
        // 취소 시에는 최대 보유금액 제한을 검사하지 않음
        this.amount += amount;
    }

}
