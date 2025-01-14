package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.dto.CouponAssignmentDto;
import kr.hhplus.be.server.domain.coupon.dto.CouponDto;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;
import kr.hhplus.be.server.domain.coupon.repository.CouponAssignmentRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouponServiceUnitTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponAssignmentRepository couponAssignmentRepository;


    private static final String TEST_USER_ID = "testUser";
    private static final long TEST_COUPON_ID = 1L;

    @Nested
    @DisplayName("쿠폰 목록 조회 테스트")
    class GetCouponsTest {

        @Test
        @DisplayName("전체 쿠폰 목록 페이징 조회 성공")
        void getAllCouponsWithPaging() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
            List<Coupon> coupons = List.of(
                    createCoupon(1L, "10% 할인 쿠폰", 1000, 100),
                    createCoupon(2L, "1000원 할인 쿠폰", 1000, 50)
            );
            Page<Coupon> couponPage = new PageImpl<>(coupons, pageable, 2);
            when(couponRepository.findAll(pageable)).thenReturn(couponPage);

            // when
            Page<CouponDto> result = couponService.getAllCoupons(pageable);

            // then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(1, result.getTotalPages());
            assertEquals(2, result.getContent().size());

            CouponDto firstCoupon = result.getContent().get(0);
            assertEquals("10% 할인 쿠폰", firstCoupon.name());
            assertEquals(1000, firstCoupon.discountAmount());
        }

        @Test
        @DisplayName("사용자의 발급된 쿠폰 목록 페이징 조회 성공")
        void getUserCouponsWithPaging() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
            List<CouponAssignment> assignments = List.of(
                    createCouponAssignment(1L, TEST_USER_ID, createCoupon(1L, "10% 할인 쿠폰", 1000, 100), CouponStatus.ISSUED),
                    createCouponAssignment(2L, TEST_USER_ID, createCoupon(2L, "1000원 할인 쿠폰", 1000, 50), CouponStatus.USED)
            );
            Page<CouponAssignment> assignmentPage = new PageImpl<>(assignments, pageable, 2);
            when(couponAssignmentRepository.findByUserId(TEST_USER_ID, pageable)).thenReturn(assignmentPage);

            // when
            Page<CouponAssignmentDto> result = couponService.getUserCoupons(TEST_USER_ID, pageable);

            // then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());

            CouponAssignmentDto firstAssignment = result.getContent().get(0);
            assertEquals(TEST_USER_ID, firstAssignment.userId ());
            assertEquals("10% 할인 쿠폰", firstAssignment.coupon().name());
        }
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class AssignCouponTest {

        @Test
        @DisplayName("쿠폰 발급 성공")
        void assignCouponSuccess() {
            // given
            Coupon coupon = createCoupon(TEST_COUPON_ID, "테스트 쿠폰", 1000, 10);
            when(couponRepository.findById(TEST_COUPON_ID)).thenReturn(Optional.of(coupon));
            when(couponAssignmentRepository.countByCouponId(TEST_COUPON_ID)).thenReturn(5);
            when(couponAssignmentRepository.existsByCouponIdAndUserId(TEST_COUPON_ID, TEST_USER_ID))
                    .thenReturn(false);
            CouponAssignment assignment = createCouponAssignment(1L, TEST_USER_ID, coupon, CouponStatus.ISSUED);
            when(couponAssignmentRepository.save(any(CouponAssignment.class)))
                    .thenReturn(assignment);

            // when
            CouponAssignmentDto result = couponService.assignCoupon(TEST_COUPON_ID, TEST_USER_ID);

            // then
            assertNotNull(result);
            assertEquals(TEST_USER_ID, result.userId());
            assertEquals(CouponStatus.ISSUED.name(), result.status());
            assertEquals(coupon.getId(), result.coupon().id());
        }

        @Test
        @DisplayName("만료된 쿠폰 발급 시도 실패")
        void assignExpiredCouponFail() {
            // given
            Coupon expiredCoupon = createExpiredCoupon(TEST_COUPON_ID, "만료된 쿠폰", 1000, 10);
            when(couponRepository.findById(TEST_COUPON_ID)).thenReturn(Optional.of(expiredCoupon));

            // when & then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> couponService.assignCoupon(TEST_COUPON_ID, TEST_USER_ID)
            );
            assertEquals("이미 만료된 쿠폰입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("이미 발급받은 쿠폰 재발급 시도 실패")
        void assignDuplicateCouponFail() {
            // given
            Coupon coupon = createCoupon(TEST_COUPON_ID, "테스트 쿠폰", 1000, 10);
            when(couponRepository.findById(TEST_COUPON_ID)).thenReturn(Optional.of(coupon));
            when(couponAssignmentRepository.existsByCouponIdAndUserId(TEST_COUPON_ID, TEST_USER_ID))
                    .thenReturn(true);

            // when & then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> couponService.assignCoupon(TEST_COUPON_ID, TEST_USER_ID)
            );
            assertEquals("이미 해당 쿠폰을 발급받았습니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 테스트")
    class UseCouponTest {

        @Test
        @DisplayName("쿠폰 사용 성공")
        void useCouponSuccess() {
            // given
            Coupon coupon = createCoupon(TEST_COUPON_ID, "테스트 쿠폰", 1000, 10);
            CouponAssignment assignment = createCouponAssignment(1L, TEST_USER_ID, coupon, CouponStatus.ISSUED);
            when(couponAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
            when(couponAssignmentRepository.save(assignment)).thenReturn(assignment);

            // when
            CouponAssignmentDto result = couponService.useCoupon(1L, TEST_USER_ID);

            // then
            assertEquals(CouponStatus.USED.name(), result.status());
            verify(couponAssignmentRepository).save(assignment);
        }

        @Test
        @DisplayName("만료된 쿠폰 사용 시도 실패")
        void useExpiredCouponFail() {
            // given
            Coupon expiredCoupon = createExpiredCoupon(TEST_COUPON_ID, "만료된 쿠폰", 1000, 10);
            CouponAssignment assignment = createCouponAssignment(1L, TEST_USER_ID, expiredCoupon, CouponStatus.ISSUED);
            when(couponAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

            // when & then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> couponService.useCoupon(1L, TEST_USER_ID)
            );
            assertEquals("만료된 쿠폰입니다.", exception.getMessage());
            assertEquals(CouponStatus.EXPIRED, assignment.getStatus());
        }
    }

    // Test Fixtures
    private Coupon createCoupon(Long id, String name, double discountAmount, int maxIssuance) {
        return Coupon.builder()
                .id(id)
                .name(name)
                .discountAmount(discountAmount)
                .maxIssuance(maxIssuance)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    private Coupon createExpiredCoupon(Long id, String name, double discountAmount, int maxIssuance) {
        return Coupon.builder()
                .id(id)
                .name(name)
                .discountAmount(discountAmount)
                .maxIssuance(maxIssuance)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private CouponAssignment createCouponAssignment(Long id, String userId, Coupon coupon, CouponStatus status) {
        return CouponAssignment.builder()
                .id(id)
                .userId(userId)
                .coupon(coupon)
                .status(status)
                .build();
    }
}
