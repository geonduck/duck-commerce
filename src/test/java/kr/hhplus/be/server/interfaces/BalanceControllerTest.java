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
public class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void chargeBalance_InvalidAmount_Returns400BadRequest() throws Exception {
        String invalidRequest = """
            {
                "userId": "user1",
                "amount": 0
            }
        """;

        mockMvc.perform(post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("금액은 최소 1 이상이어야 합니다."));
    }

    @Test
    public void chargeBalance_ValidRequest_Returns200Ok() throws Exception {
        String validRequest = """
            {
                "userId": "user1",
                "amount": 10000
            }
        """;

        mockMvc.perform(post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}