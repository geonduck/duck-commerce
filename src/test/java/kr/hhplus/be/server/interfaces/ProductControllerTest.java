package kr.hhplus.be.server.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.Stock;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private Product testProduct;

    @BeforeEach
    void setup() {
        // Redis 데이터 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 데이터베이스 초기화
        entityManager.createQuery("DELETE FROM Stock").executeUpdate();
        entityManager.createQuery("DELETE FROM Product").executeUpdate();
        entityManager.flush();

        // 테스트 제품 초기 데이터 추가
        testProduct = Product.builder()
                .name("Test Product")
                .price(1000)
                .build();

        testProduct = productService.saveProduct(testProduct); // Save 테스트 데이터를 DB에 저장

        // 테스트 재고 초기 데이터 추가
        Stock testStock = Stock.builder()
                .productId(testProduct.getId())
                .quantity(10)
                .build();

        stockService.save(testStock); // Save 테스트 데이터를 DB에 저장
    }


    @Test
    public void getProduct_InvalidProductId_ShouldReturn400() throws Exception {
        // 유효하지 않은 Product ID
        mockMvc.perform(get("/api/v1/products/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("상품 ID는 1 이상이어야 합니다."));
    }

    @Test
    public void getProductList_ShouldReturn200() throws Exception {
        // 정상 호출 테스트
        mockMvc.perform(get("/api/v1/products/"))
                .andExpect(status().isOk());
    }
}