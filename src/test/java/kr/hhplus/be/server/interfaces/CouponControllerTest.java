package kr.hhplus.be.server.interfaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void save_InvalidCouponId_ShouldReturn400() throws Exception {
        // 유효하지 않은 쿠폰 ID
        String invalidRequest = """
            {
                "userId": "user1",
                "couponId": 0
            }
        """;

        mockMvc.perform(post("/api/v1/coupons/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("쿠폰 ID는 반드시 1 이상의 값이어야 합니다."));
    }

    @Test
    public void save_ValidRequest_ShouldReturn200() throws Exception {
        // 올바른 요청 데이터
        String validRequest = """
            {
                "userId": "user1",
                "couponId": 100
            }
        """;

        mockMvc.perform(post("/api/v1/coupons/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}