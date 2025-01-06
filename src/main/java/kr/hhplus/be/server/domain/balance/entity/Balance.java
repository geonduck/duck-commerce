package kr.hhplus.be.server.domain.balance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.BaseTimeEntity;
import lombok.*;

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
            throw new IllegalStateException("최대 보유 가능 금액은 10,000,000원 입니다");
        }
        this.amount += amount;
    }

    public void use(double amount) {
        if (this.amount - amount < 0) {
            throw new IllegalStateException("잔고가 부족하여 사용이 불가능 합니다");
        }
        this.amount -= amount;
    }

    public void cancelUse(double amount) {
        // 취소 시에는 최대 보유금액 제한을 검사하지 않음
        this.amount += amount;
    }

}
