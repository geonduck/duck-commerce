package kr.hhplus.be.server.interfaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getPayment_InvalidPaymentId_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/payments/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("결제 ID는 1 이상의 값이어야 합니다."));
    }

    @Test
    public void createPayment_InvalidUserId_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/payments/pay")
                        .param("userId", "")
                        .param("orderId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사용자 ID는 필수입니다."));
    }

    @Test
    public void createPayment_InvalidOrderId_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/payments/pay")
                        .param("userId", "user123")
                        .param("orderId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("주문 ID는 1 이상의 값이어야 합니다."));
    }
}