package kr.hhplus.be.server.facade.payment;

import kr.hhplus.be.server.domain.balance.dto.BalanceDomainDto;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.dto.OrderItemResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.payment.dto.PaymentDomainDto;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.product.service.ProductDailySalesService;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final BalanceService balanceService;
    private final ProductDailySalesService productDailySalesService;

    /**
     * 유저의 주문을 가져와 결제를 생성
     */
    public PaymentResponseDto createPayment(String userId, Long orderId) {
        // 1. 유저의 주문 데이터를 가져오기
        Order order = orderService.findOrderById(orderId);
        double paymentAmount = order.getTotalAmount();

        // 2. 결제 생성
        PaymentDomainDto payment = paymentService.createPayment(orderId, paymentAmount);

        // 3. 유저의 잔액 차감
        BalanceDomainDto balanceUpdateRequest = new BalanceDomainDto(userId, paymentAmount);
        balanceService.use(balanceUpdateRequest); // 여기서 잔액이 부족하면 예외 발생

        // 4. 결제 상태를 COMPLETED로 설정해 업데이트
        updatePaymentStatus(payment, PaymentStatus.COMPLETED);

        // 5. PaymentDomainDto → PaymentResponseDto로 변환 후 반환
        return toPaymentResponseDto(payment);
    }

    /**
     * 결제 상태 업데이트 및 관련 비즈니스 로직 실행
     */
    public void updatePaymentStatus(PaymentDomainDto payment, PaymentStatus status) {
        // 1. 상태 업데이트
        paymentService.updatePaymentStatus(payment.id(), status);

        if (status == PaymentStatus.COMPLETED) {
            orderService.updateOrderStatus(payment.orderId(), OrderStatus.COMPLETED);
            reflectDailySales(payment);
        } else if (status == PaymentStatus.CANCELED) {
            orderService.updateOrderStatus(payment.orderId(), OrderStatus.CANCELED);
        } else if (status == PaymentStatus.FAILED) {
            orderService.updateOrderStatus(payment.orderId(), OrderStatus.PAYMENT_FAILED);
        }

        // 2. 결제 완료 후 추가 비즈니스 로직 실행 (ProductDailySales 갱신)
        if (status == PaymentStatus.COMPLETED) {
            reflectDailySales(payment);
        }
    }

    /**
     * 결제 조회
     */
    public PaymentResponseDto getPayment(Long paymentId) {
        PaymentDomainDto payment = paymentService.getPayment(paymentId);
        return toPaymentResponseDto(payment);
    }

    /**
     * 상품의 일일 판매량 반영 (주문 아이템 기준)
     */
    private void reflectDailySales(PaymentDomainDto payment) {
        List<OrderItemResponse> orderItems = orderService.getOrderItems(payment.orderId());

        for (OrderItemResponse item : orderItems) {
            productDailySalesService.updateDailySales(item.productId(), item.amount());
        }
    }

    public List<PaymentResponseDto> getPaymentListByUser(String userId, Pageable pageable) {
        // 1. PaymentService에서 결제 리스트 조회
        Page<PaymentDomainDto> paymentPage = paymentService.getPaymentsByUser(userId, pageable);

        // 2. DTO 변환 및 반환
        return paymentPage.map(this::toPaymentResponseDto).getContent();
    }

    /**
     * PaymentDomainDto를 PaymentResponseDto로 변환
     */
    private PaymentResponseDto toPaymentResponseDto(PaymentDomainDto domainDto) {
        return new PaymentResponseDto(
                "unknown", // userId는 domainDto에 없으니 기본값 설정
                domainDto.orderId().intValue(),
                domainDto.status().name()
        );
    }
}