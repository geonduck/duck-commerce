package kr.hhplus.be.server.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.interfaces.payment.dto.PaymentRequestDto;
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
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getPayment_InvalidPaymentId_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/payments/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("결제 ID는 1 이상이어야 합니다."));
    }

    @Test
    public void createPayment_InvalidUserId_ShouldReturn400() throws Exception {
        PaymentRequestDto invalidRequest = new PaymentRequestDto("", 10L);

        mockMvc.perform(post("/api/v1/payments/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사용자 ID는 필수입니다."));
    }

    @Test
    public void createPayment_InvalidOrderId_ShouldReturn400() throws Exception {
        PaymentRequestDto invalidRequest = new PaymentRequestDto("user123", 0L);

        mockMvc.perform(post("/api/v1/payments/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("주문 ID는 1 이상의 값이어야 합니다."));
    }
}