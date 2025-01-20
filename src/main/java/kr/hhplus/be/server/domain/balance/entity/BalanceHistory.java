package kr.hhplus.be.server.domain.balance.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseTimeEntity;
import kr.hhplus.be.server.domain.balance.BalanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class BalanceHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;  // 사용자 ID
    private double amountChanged;   // 변경 금액

    @Enumerated(EnumType.STRING)
    private BalanceStatus status;    // 변화 상태
}
