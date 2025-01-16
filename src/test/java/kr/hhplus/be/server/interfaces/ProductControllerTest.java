package kr.hhplus.be.server.interfaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getProduct_InvalidProductId_ShouldReturn400() throws Exception {
        // 유효하지 않은 Product ID
        mockMvc.perform(get("/api/v1/products/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("상품 ID는 1 이상의 양수여야 합니다."));
    }

    @Test
    public void getProduct_NullProductId_ShouldReturn400() throws Exception {
        // NULL을 의도해서 전달
        mockMvc.perform(get("/api/v1/products/"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void getProductList_ShouldReturn200() throws Exception {
        // 정상 호출 테스트
        mockMvc.perform(get("/api/v1/products/"))
                .andExpect(status().isOk());
    }
}