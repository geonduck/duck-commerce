package kr.hhplus.be.server.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.balance.entity.Balance;
import kr.hhplus.be.server.domain.balance.service.BalanceService;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.interfaces.balance.dto.BalanceRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BalanceService balanceService;@PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setup() {

        // 데이터베이스 초기화
        entityManager.createQuery("DELETE FROM Balance").executeUpdate();
        entityManager.flush();

        String testUser = "user1";
        balanceService.save(Balance.builder().userId(testUser).amount(50_000).build());
    }
    @Test
    public void chargeBalance_InvalidAmount_Returns400BadRequest() throws Exception {
        BalanceRequestDto invalidRequest = new BalanceRequestDto("user1", 0);


        mockMvc.perform(post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("금액은 최소 1 이상이어야 합니다."));
    }

    @Test
    public void chargeBalance_ValidRequest_Returns200Ok() throws Exception {
        BalanceRequestDto validRequest = new BalanceRequestDto("user1", 10000);

        mockMvc.perform(post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}