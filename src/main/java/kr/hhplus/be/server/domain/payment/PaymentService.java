package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.payment.dto.PaymentDomainDto;
import kr.hhplus.be.server.domain.payment.entity.Payment;
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
    public PaymentDomainDto createPayment(Long orderId, double paymentAmount) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentAmount(paymentAmount)
                .paymentStatus(Payment.PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentDomainDto.of(savedPayment);
    }

    /**
     * 결제 상태 업데이트
     */
    @Transactional
    public void updatePaymentStatus(Long paymentId, Payment.PaymentStatus status) {
        Payment payment = findPaymentById(paymentId);

        payment.setPaymentStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (status == Payment.PaymentStatus.COMPLETED) {
            reflectDailySales(payment.getOrderId());
        }
    }

    private void reflectDailySales(Long orderId) {
        List<OrderItemResponse> orderItems = orderService.getOrderItems(orderId);

        for (OrderItemResponse item : orderItems) {
            productDailySalesService.updateDailySales(item.productId(), item.amount());
        }
    }

    @Transactional(readOnly = true)
    public PaymentDomainDto getPayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        return PaymentDomainDto.of(payment);
    }

    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다. ID: " + paymentId));
    }
}