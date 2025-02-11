package kr.hhplus.be.server.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.interfaces.coupon.dto.CouponRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponService couponService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private Coupon testCoupon;

    @BeforeEach
    void setup() {
        // Redis 데이터 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 데이터베이스 초기화
        entityManager.createQuery("DELETE FROM CouponAssignment").executeUpdate();
        entityManager.createQuery("DELETE FROM Coupon").executeUpdate();
        entityManager.flush();

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
    @DisplayName("유효하지 않은 쿠폰 ID로 쿠폰 발급 시도 시 400 반환")
    public void save_InvalidCouponId_ShouldReturn400() throws Exception {
        // 유효하지 않은 쿠폰 ID
        CouponRequestDto invalidRequest = new CouponRequestDto("user1", 0);

        mockMvc.perform(post("/api/v1/coupons/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("쿠폰 ID는 반드시 1 이상의 값이어야 합니다."));
    }

    @Test
    @DisplayName("유효한 요청으로 쿠폰 발급 시도 시 200 반환")
    @Sql({"/insert.sql"})
    public void save_ValidRequest_ShouldReturn200() throws Exception {
        // 올바른 요청 데이터
        CouponRequestDto validRequest = new CouponRequestDto("user1", testCoupon.getId());

        mockMvc.perform(post("/api/v1/coupons/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("특정 유저의 쿠폰 조회 성공")
    public void getUserCoupons_ShouldReturn200() throws Exception {
        // given
        String userId = "user1";
        couponService.assignCoupon(testCoupon.getId(), userId);

        // when & then
        mockMvc.perform(get("/api/v1/coupons/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].coupon_id").value(testCoupon.getId()));
    }

    @Test
    @DisplayName("발급 가능한 쿠폰 리스트 조회 성공")
    public void getAvailableCoupons_ShouldReturn200() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/coupons/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].coupon_id").value(testCoupon.getId()));
    }
}