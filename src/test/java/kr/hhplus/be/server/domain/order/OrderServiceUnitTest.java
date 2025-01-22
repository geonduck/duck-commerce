package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.order.dto.OrderCalculationResult;
import kr.hhplus.be.server.domain.order.dto.OrderResponse;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 아이템 저장 및 주문 생성 테스트")
    void createOrderTest() {
        // Given
        String userId = "testUser";

        OrderItem item1 = OrderItem.builder()
                .productId(1L)
                .userId(userId)
                .amount(2)
                .productName("상품 A")
                .price(100.0)
                .build();

        OrderItem item2 = OrderItem.builder()
                .productId(2L)
                .userId(userId)
                .amount(1)
                .productName("상품 B")
                .price(200.0)
                .build();

        List<OrderItem> orderItems = List.of(item1, item2);
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(400.0)
                .discountAmount(0.0)
                .orderStatus(OrderStatus.PENDING)
                .build();
        OrderCalculationResult result = new OrderCalculationResult(400.0, orderItems);

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.saveAll(anyList())).thenReturn(orderItems);

        // When
        OrderResponse response = orderService.createOrder(userId, result);

        // Then
        assertEquals(userId, response.userId());
        assertEquals(2, response.orderItems().size());
        assertEquals(400.0, response.totalAmount());

        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("유효하지 않은 주문 ID로 주문 조회 시 예외 발생")
    void findOrderByInvalidIdTest() {
        // Given
        Long invalidOrderId = 999L;

        lenient().when(orderRepository.findById(invalidOrderId)).thenReturn(java.util.Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class,
                () -> orderService.findByOrderId(invalidOrderId));
        assertEquals(OrderErrorCode.NOT_FIND_EXCEPTION.getMessage(), exception.getMessage());

    }

    @Test
    @DisplayName("주문 상태 변경 성공")
    void testUpdateOrderStatus_Success() {
        // Arrange
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.COMPLETED;
        Order mockOrder = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act
        orderService.updateOrderStatus(orderId, newStatus);

        // Assert
        assertThat(mockOrder.getOrderStatus()).isEqualTo(newStatus);
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    @DisplayName("존재하지 않는 주문 상태 변경 시 실패")
    void testUpdateOrderStatus_OrderNotFound() {
        // Arrange
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED))
                .isInstanceOf(DomainException.class)
                .hasMessage(OrderErrorCode.NOT_FIND_EXCEPTION.getMessage());
    }
}