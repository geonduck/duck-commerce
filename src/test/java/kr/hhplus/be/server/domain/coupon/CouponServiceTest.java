package kr.hhplus.be.server.domain.coupon;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.DomainException;
import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.dto.CouponDto;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private SetOperations<String, Object> setOperations;

    private static final String TEST_USER_ID = "testUser";
    private static final String ANOTHER_USER_ID = "anotherUser";

    private Coupon testCoupon;

    @BeforeEach
    void setup() {
        setOperations = redisTemplate.opsForSet();

        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 테스트 쿠폰 초기 데이터 추가
        testCoupon = Coupon.builder()
                .name("Test Coupon")
                .discountAmount(1000)
                .maxIssuance(5)
                .expiredAt(LocalDateTime.now().plusDays(7)) // 7일 뒤 만료
                .build();

        testCoupon = couponService.save(testCoupon); // Save 테스트 데이터를 DB에 저장
    }

    @Test
    @DisplayName("전체 쿠폰 페이징 목록 조회 성공")
    void testGetAllCoupons_Paging() {
        // given
        PageRequest pageable = PageRequest.of(0, 10); // 첫 페이지, 10개씩 조회

        // when
        Page<CouponDto> result = couponService.getAllCoupons(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo(testCoupon.getName());
    }

    @Test
    @DisplayName("쿠폰 발급 성공")
    void testAssignCoupon_Success() {
        // given
        long couponId = testCoupon.getId();

        // when
        CouponAssignmentDto result = couponService.assignCoupon(couponId, TEST_USER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(TEST_USER_ID);
        assertThat(result.coupon().id()).isEqualTo(couponId);
        assertThat(result.status()).isEqualTo("ISSUED");
    }

    @Test
    @DisplayName("이미 발급된 쿠폰 재발급 시도 실패")
    void testAssignCoupon_AlreadyAssigned() {
        // given
        long couponId = testCoupon.getId();
        couponService.assignCoupon(couponId, TEST_USER_ID); // 첫 발급

        // when & then
        assertThatThrownBy(() -> couponService.assignCoupon(couponId, TEST_USER_ID))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("이미 해당 쿠폰을 발급받았습니다");
    }

    @Test
    @DisplayName("만료된 쿠폰 발급 시도 실패")
    void testAssignCoupon_Expired() {
        // given
        Coupon expiredCoupon = Coupon.builder()
                .name("Expired Coupon")
                .discountAmount(500)
                .maxIssuance(10)
                .expiredAt(LocalDateTime.now().minusDays(1)) // 만료
                .build();
        expiredCoupon = couponService.save(expiredCoupon);

        long couponId = expiredCoupon.getId();

        // when & then
        assertThatThrownBy(() -> couponService.assignCoupon(couponId, TEST_USER_ID))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("이미 만료된 쿠폰입니다");
    }

    @Test
    @DisplayName("쿠폰 사용 성공")
    void testUseCoupon_Success() {
        // given
        long couponId = testCoupon.getId();
        CouponAssignmentDto assignedCoupon = couponService.assignCoupon(couponId, TEST_USER_ID);

        // when
        CouponAssignmentDto result = couponService.useCoupon(assignedCoupon.id(), TEST_USER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("USED");
    }

    @Test
    @DisplayName("다른 사용자의 쿠폰 사용 시도 실패")
    void testUseCoupon_NotOwner() {
        // given
        long couponId = testCoupon.getId();
        CouponAssignmentDto assignedCoupon = couponService.assignCoupon(couponId, TEST_USER_ID);

        // when & then
        assertThatThrownBy(() -> couponService.useCoupon(assignedCoupon.id(), ANOTHER_USER_ID))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("해당 사용자의 쿠폰이 아닙니다");
    }

    @Test
    @DisplayName("사용자 발급 쿠폰 목록 조회 성공")
    void testGetUserCoupons_Paging() {
        // given
        long couponId = testCoupon.getId();
        couponService.assignCoupon(couponId, TEST_USER_ID); // 쿠폰 발급
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<CouponAssignmentDto> result = couponService.getUserCoupons(TEST_USER_ID, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).userId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getContent().get(0).coupon().name()).isEqualTo(testCoupon.getName());
    }
}