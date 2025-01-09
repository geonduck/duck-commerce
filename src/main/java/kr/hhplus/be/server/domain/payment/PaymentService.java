package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.entity.Payment.PaymentStatus;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.product.ProductDailySalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProductDailySalesService productDailySalesService;
    private final OrderService orderService;

    /**
     * 새로운 결제 생성
     */
    @Transactional
    public Payment createPayment(Long orderId, double paymentAmount) {
        // 결제 엔티티 생성
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentAmount(paymentAmount)
                .paymentStatus(PaymentStatus.PENDING) // 초기 상태는 PENDING
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 저장 후 반환
        return paymentRepository.save(payment);
    }

    /**
     * 결제 상태 업데이트
     */
    @Transactional
    public void updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = findPaymentById(paymentId);

        // 상태 업데이트 및 수정 시간 기록
        payment.setPaymentStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        // 결제 완료 시 일일 판매량 반영
        if (status == PaymentStatus.COMPLETED) {
            reflectDailySales(payment.getOrderId());
        }

    }

    private void reflectDailySales(Long orderId) {
        // 주문의 상품 정보 조회
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);

        // 상품별 판매량 업데이트
        for (OrderItem item : orderItems) {
            productDailySalesService.updateDailySales(item.getProductId(), item.getAmount());
        }
    }


    /**
     * 결제 조회
     */
    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId) {
        return findPaymentById(paymentId);
    }

    /**
     * 특정 주문의 결제 조회
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문에 대한 결제가 없습니다."));
    }


    /**
     * 결제 ID로 Payment 엔티티 조회
     */
    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다. ID: " + paymentId));
    }
}
