package kr.hhplus.be.server.facade.coupon;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponRequestDto;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponConcurrencyTest {

    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private CouponService couponService;

    @Test
    @DisplayName("동시에 여러 사용자가 동일 쿠폰 발급: maxIssuance 초과 금지")
    void testConcurrentCouponAssign() {
        // given
        Coupon coupon = Coupon.builder()
                .name("Concurrent Test Coupon")
                .discountAmount(1000)
                .maxIssuance(5)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        coupon = couponService.save(coupon);
        long couponId = coupon.getId();

        int threadCount = 10;

        CompletableFuture<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    String userId = "user" + i;
                    try {
                        couponFacade.assignCoupon(new CouponRequestDto(userId, couponId));
                    } catch (Exception e) {
                        // Ignore exceptions for the purpose of this test
                    }
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        // when
        int issuedCount = (int) couponService.countByCouponId(couponId);

        // then
        assertThat(issuedCount).isEqualTo(coupon.getMaxIssuance());
    }

    @Test
    @DisplayName("동시에 여러 사용자가 자신의 쿠폰 목록 조회")
    void testConcurrentGetUserCoupons() {
        // given
        Coupon coupon = Coupon.builder()
                .name("Concurrent Test Coupon")
                .discountAmount(1000)
                .maxIssuance(10)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        coupon = couponService.save(coupon);
        long couponId = coupon.getId();

        int threadCount = 10;
        IntStream.range(0, threadCount).forEach(i -> {
            String userId = "user" + i;
            couponFacade.assignCoupon(new CouponRequestDto(userId, couponId));
        });

        CompletableFuture<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    String userId = "user" + i;
                    List<CouponResponseDto> userCoupons = couponFacade.getUserCoupons(userId, PageRequest.of(0, 10));
                    assertThat(userCoupons).isNotEmpty();
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    @Test
    @DisplayName("동시에 여러 사용자가 사용 가능한 쿠폰 목록 조회")
    void testConcurrentGetAvailableCoupons() {
        // given
        Coupon coupon = Coupon.builder()
                .name("Available Test Coupon")
                .discountAmount(1000)
                .maxIssuance(10)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        coupon = couponService.save(coupon);

        int threadCount = 10;

        CompletableFuture<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    List<CouponResponseDto> availableCoupons = couponFacade.getAvailableCoupons(PageRequest.of(0, 10));
                    assertThat(availableCoupons).isNotEmpty();
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    @Test
    @DisplayName("동시에 여러 사용자가 쿠폰 발급 및 발급 가능한 쿠폰 목록 조회")
    void testConcurrentAssignAndGetAvailableCoupons() {
        // given
        Coupon coupon = Coupon.builder()
                .name("Concurrent Test Coupon")
                .discountAmount(1000)
                .maxIssuance(10)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        coupon = couponService.save(coupon);
        long couponId = coupon.getId();

        int threadCount = 10;

        CompletableFuture<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    String userId = "user" + i;
                    try {
                        couponFacade.assignCoupon(new CouponRequestDto(userId, couponId));
                    } catch (Exception e) {
                        // Ignore exceptions for the purpose of this test
                    }
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture<?>[] queryFutures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    List<CouponResponseDto> availableCoupons = couponFacade.getAvailableCoupons(PageRequest.of(0, 10));
                    assertThat(availableCoupons).isNotEmpty();
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        CompletableFuture.allOf(queryFutures).join();

        // when
        int issuedCount = (int) couponService.countByCouponId(couponId);

        // then
        assertThat(issuedCount).isEqualTo(coupon.getMaxIssuance());
    }

}