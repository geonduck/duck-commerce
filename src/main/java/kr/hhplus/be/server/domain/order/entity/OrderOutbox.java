package kr.hhplus.be.server.domain.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseTimeEntity;
import kr.hhplus.be.server.domain.order.OrderEventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "outbox_event", indexes = {
        @Index(name = "idx_status_created_date", columnList = "status, createdDate"),
        @Index(name = "idx_message_id", columnList = "messageId")
})
@Entity
public class OrderOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private Long messageId; // 주문 ID

    @Column(nullable = false)
    private String topic;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // Kafka로 전송할 JSON 데이터

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderEventStatus status;

    public void updateStatus(OrderEventStatus status) {
        this.status = status;
    }

}
