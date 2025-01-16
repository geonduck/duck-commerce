package kr.hhplus.be.server.domain.order.dto;

import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.order.OrderErrorCode;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.product.dto.ProductDomainDto;

import java.util.ArrayList;
import java.util.List;

public record OrderCalculationResult(double totalAmount, List<OrderItem> orderItems) {
    public static OrderCalculationResult calculateOrderItemsAndTotalAmount(
            List<OrderItemRequest> items, List<ProductDomainDto> products, String userId) {
        // OrderItem 생성 및 총 금액 계산
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemRequest item : items) {
            // 해당 아이템에 해당하는 상품 정보 검색
            ProductDomainDto product = products.stream().filter(p -> p.id().equals(item.productId())).findFirst().orElseThrow(() -> new DomainException(OrderErrorCode.NOT_FOUND_PRODUCT_EXCEPTION));

            // 상품에 대한 OrderItem 생성
            OrderItem orderItem = OrderItem.builder().productId(product.id()).userId(userId).amount(item.amount()).productName(product.name()) // 상품 이름
                    .price(product.price())     // 상품 단가
                    .build();
            orderItems.add(orderItem);

            // 총금액 누적 계산
            totalAmount += item.amount() * product.price();
        }

        // 총 금액 및 생성된 OrderItems 반환
        return new OrderCalculationResult(totalAmount, orderItems);
    }
}
