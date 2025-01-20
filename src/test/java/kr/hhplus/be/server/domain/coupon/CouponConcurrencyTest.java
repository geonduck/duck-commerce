package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Test
    @DisplayName("동시에 여러 사용자가 동일 쿠폰 발급: maxIssuance 초과 금지")
    void testConcurrentCouponAssign() throws InterruptedException {
        // given
        // 5장의 쿠폰만 발급 가능하도록 설정
        Coupon coupon = Coupon.builder()
                .name("Concurrent Test Coupon")
                .discountAmount(1000)
                .maxIssuance(5) // 최대 5장 발급 가능
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        coupon = couponService.save(coupon);
        long couponId = coupon.getId();

        int threadCount = 10; // 10개의 스레드 동시 실행
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 각 스레드에서 수행할 작업: Coupon을 assign
        for (int i = 0; i < threadCount; i++) {
            final int userIdSuffix = i; // 서로 다른 사용자 ID 생성: user0, user1, ...
            executorService.execute(() -> {
                try {
                    String userId = "user" + userIdSuffix;
                    couponService.assignCoupon(couponId, userId);
                } catch (Exception e) {
                    // 쿠폰 초과나 기타 예외 무시 (테스트 목적: 정상 동작 확인)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 작업 완료할 때까지 대기
        executorService.shutdown();

        // when
        // 발급된 총 쿠폰 수 확인
        int issuedCount = (int) couponService.countByCouponId(couponId);

        // then
        assertThat(issuedCount).isEqualTo(coupon.getMaxIssuance()); // 최대 발급량 이하여야 함
    }

    @Test
    @DisplayName("동시에 여러 사용자가 동일 쿠폰 사용: 상태 변경 충돌 방지")
    void testConcurrentCouponUse() throws InterruptedException {
        // given
        Coupon coupon = Coupon.builder()
                .name("Use Test Coupon")
                .discountAmount(1000)
                .maxIssuance(1)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        coupon = couponService.save(coupon);

        // 쿠폰 발급 (user1에게 발급)
        CouponAssignmentDto assignment = couponService.assignCoupon(coupon.getId(), "user1");

        int threadCount = 5; // 5개의 스레드 동시 실행
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 각 스레드에서 쿠폰 사용 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    couponService.useCoupon(assignment.id(), "user1");
                } catch (Exception e) {
                    // 쿠폰 이미 사용되었거나, 기타 예외 발생
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 작업 완료될 때까지 대기
        executorService.shutdown();

        // when
        // 쿠폰 상태 확인
        String status = couponService.getUserCoupons("user1", null)
                .getContent().get(0).status();

        // then
        assertThat(status).isEqualTo("USED"); // 상태는 정확히 USED가 되어야 함

        // 쿠폰 사용 시 한 번만 성공했는지 검증
        long usedCount = couponService.getUserCoupons("user1", null).stream()
                .filter(couponAssignment -> couponAssignment.status().equals("USED"))
                .count();

        assertThat(usedCount).isEqualTo(1); // 1번만 성공적으로 사용됨
    }
}