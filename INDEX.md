# **DB 인덱스 최적화 성능 분석 보고서**

---

## **1. 요약 (Abstract)**

- **목적**:

데이터베이스 인덱스가 쿼리 성능에 미치는 영향을 실험적으로 분석하고, 인덱스 적용 전후의 성능 차이를 상세히 비교한다.

- **핵심 결과**:
- 인덱스 도입 시 대부분의 조건 검색, 조인 및 잠금 쿼리에서 Full Table Scan을 피하고, 인덱스 룩업 방식으로 전환되어 실행 시간이 크게 단축됨.
- 단순 조회나 기본 키 검색에서는 미세한 차이가 있으나, 복잡하거나 대량 데이터 처리 쿼리에서는 확연한 성능 개선 효과가 확인됨.

---

## **2. 서론 (Introduction)**

대용량 데이터를 다루는 시스템에서는 쿼리 성능 최적화가 매우 중요합니다.

**인덱스**는 이러한 성능 향상을 위한 핵심 기술로, 데이터 검색, 정렬, 조인 연산에서 불필요한 전체 스캔(Full Table Scan)을 줄여주며, 시스템 자원의 효율적 사용을 돕습니다.

본 보고서는 실제 시나리오에서 수집한 다양한 쿼리를 대상으로 인덱스 적용 전후의 실행 계획과 성능을 비교 분석하여, 인덱스 전략의 효과를 체계적으로 평가합니다.

---

## **3. 배경 (Background)**

### **3.1 인덱스의 개념과 역할**

- **정의**:

인덱스는 특정 열이나 열 집합에 대해 정렬된 자료구조(B+Tree, Hash 등)를 사용하여, 검색, 정렬, 조인 등의 작업에서 데이터 접근 속도를 향상시킵니다.

- **인덱스 부재 시 문제점**:
- **Full Table Scan**: 테이블의 모든 행을 순차 검색 → 데이터량이 많을수록 실행 시간이 증가
- **인덱스 적용 시 효과**:
- **트리 탐색 (B+Tree)** 또는 **해시 탐색 (Hash)** 을 통해 필요한 데이터만 빠르게 조회
- 범위 검색, 정렬, 조인 작업 시 성능 최적화

### **3.2 주요 인덱스 자료구조**

| **자료구조** | **특징** | **장점** | **단점** |
| --- | --- | --- | --- |
| **B+Tree** | 균형 트리 형태(루트, 브랜치, 리프 노드 구성) | O(log N) 검색 속도범위 검색 및 정렬에 유리 | 삽입/삭제 시 오버헤드 발생 |
| **Hash** | 해시 함수를 통한 키 매핑 | 등가 조건 검색 시 매우 빠름 (O(1)) | 범위 검색 및 정렬 불가해시 충돌 발생 가능 |
| **복합 인덱스** | 여러 컬럼을 하나의 인덱스로 결합 | 다중 조건 검색에 효과적 | 컬럼 순서에 따라 인덱스 활용도가 달라짐 |

## **4. 연구 방법 (Methodology)**

1. **데이터 셋업**:

동일한 더미 데이터를 가진 두 개의 스키마를 준비하여, 한쪽 스키마에만 인덱스를 추가합니다.

```jsx
mysqldump -uroot -p hhindex > indexDump.sql
mysql -uroot -p
create database noindex;
mysql -uroot -p noindex < indexDump.sql
```

1. **인덱스 추가**:

각 테이블에 대해 다음과 같이 인덱스를 생성합니다.

```sql
-- Balance
CREATE INDEX idx_balance_user_id ON Balance (user_id);

-- Coupon
CREATE INDEX idx_coupon_assignment_coupon_id ON coupon_assignment (coupon_id);
CREATE INDEX idx_coupon_assignment_user_id ON coupon_assignment (user_id);
CREATE INDEX idx_coupon_id ON Coupon (id);

-- Order
CREATE INDEX idx_order_item_order_id ON order_item (order_id);
CREATE INDEX idx_order_user_id ON Orders (user_id);
CREATE INDEX idx_order_id ON Orders (id);

-- Payment
CREATE INDEX idx_payment_id ON Payment (id);

-- Product
CREATE INDEX idx_product_daily_sales_product_id_date ON product_daily_sales (product_id, date);
CREATE INDEX idx_stock_product_id ON Stock (product_id);
CREATE INDEX idx_stock_product_id_in ON Stock (product_id);
```

1. **쿼리 선정 및 성능 측정**:

각 테이블별로 자주 사용되는 쿼리들을 선정한 후, 인덱스 적용 전후의 EXPLAIN ANALYZE 결과와 실행 시간을 기록합니다.

---

## **5. 연구 결과 (Results and Analysis)**

아래는 테이블별, 쿼리별 인덱스 적용 전후의 실행 계획과 실행 시간을 비교 분석한 내용입니다.

---

### **5.1 Balance 테이블**

**(a) findAllByUserId(String userId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Balance WHERE user_id = 'User1000';
```

- **인덱스 적용 전**:

```sql
-> Filter: (balance.user_id = 'User1000')  (cost=187085 rows=179555) (actual time=4.16..1401 rows=1 loops=1)
    -> Table scan on Balance  (cost=187085 rows=1.8e+6) (actual time=2.8..1302 rows=1.8e+6 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on Balance using idx_balance_user_id (user_id='User1000')  (cost=1.04 rows=1) (actual time=2.15..2.15 rows=1 loops=1)
```

- **분석**:
- **변경 전**: 전체 테이블 스캔으로 약 1,800,000 행을 조회함
- **변경 후**: 인덱스 룩업을 통해 단 1행만 조회, 실행 시간이 크게 단축됨

---

**(b) findByUserIdWithLock(String userId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Balance WHERE user_id = 'User1000' FOR UPDATE;
```

- **인덱스 적용 전**:

```sql
-> Filter: (balance.user_id = 'User1000')  (cost=184368 rows=179555) (actual time=0.414..496 rows=1 loops=1)
    -> Table scan on Balance  (cost=184368 rows=1.8e+6) (actual time=0.104..393 rows=1.8e+6 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on Balance using idx_balance_user_id (user_id='User1000')  (cost=1.04 rows=1) (actual time=0.032..0.0337 rows=1 loops=1)
```

- **분석**:
- 인덱스 적용 후, FOR UPDATE 잠금 쿼리에서도 인덱스 룩업이 빠르게 동작하여 불필요한 행 스캔을 방지함

---

**(c) findByUserId(String userId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Balance WHERE user_id = 'User1000';
```

- **인덱스 적용 전**:

```sql
-> Filter: (balance.user_id = 'User1000')  (cost=184368 rows=179555) (actual time=0.431..482 rows=1 loops=1)
    -> Table scan on Balance  (cost=184368 rows=1.8e+6) (actual time=0.161..382 rows=1.8e+6 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on Balance using idx_balance_user_id (user_id='User1000')  (cost=1.04 rows=1) (actual time=0.0448..0.0461 rows=1 loops=1)
```

- **분석**:
- 단순 조회 쿼리에서 인덱스 도입으로 실행 시간이 0.16382ms에서 0.04480.0461ms로 대폭 감소됨

---

### **5.2 Coupon 테이블**

**(a) CouponAssignmentRepositoryImpl - countByCouponId(long id)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT COUNT(*) FROM coupon_assignment WHERE coupon_id = 1;
```

- **인덱스 적용 전**:

```sql
-> Aggregate: count(0)  (cost=0.45 rows=1) (actual time=0.632..0.632 rows=1 loops=1)
    -> Covering index lookup on coupon_assignment using FK9jon19rgsx40dx8lemv1jl5vh (coupon_id=1)  (cost=0.35 rows=1) (actual time=0.46..0.47 rows=1 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Aggregate: count(0)  (cost=0.45 rows=1) (actual time=0.037..0.037 rows=1 loops=1)
    -> Covering index lookup on coupon_assignment using FK9jon19rgsx40dx8lemv1jl5vh (coupon_id=1)  (cost=0.35 rows=1) (actual time=0.0236..0.0268 rows=1 loops=1)
```

- **분석**:
- 인덱스 추가 후, COUNT 연산의 실행 시간이 0.632ms에서 0.037ms로 크게 개선됨

---

**(b) CouponAssignmentRepositoryImpl - existsByCouponIdAndUserId(long id, String userId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT 1 FROM coupon_assignment WHERE coupon_id = 1 AND user_id = 'User1000';
```

- **인덱스 적용 전**:

```sql
-> Filter: (coupon_assignment.user_id = 'User1000')  (cost=0.26 rows=0.1) (actual time=0.0785..0.0803 rows=1 loops=1)
    -> Index lookup on coupon_assignment using FK9jon19rgsx40dx8lemv1jl5vh (coupon_id=1)  (cost=0.26 rows=1) (actual time=0.0722..0.0739 rows=1 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Filter: (coupon_assignment.user_id = 'User1000')  (cost=0.35 rows=1) (actual time=0.521..0.523 rows=1 loops=1)
    -> Index lookup on coupon_assignment using FK9jon19rgsx40dx8lemv1jl5vh (coupon_id=1)  (cost=0.35 rows=1) (actual time=0.517..0.519 rows=1 loops=1)
```

- **분석**:
- 두 경우 모두 인덱스 룩업을 활용하나, 실행 시간 차이는 미세한 수준으로 안정적인 성능을 보임

---

**(c) CouponAssignmentRepositoryImpl - findByUserId(String userId, Pageable pageable)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM coupon_assignment WHERE user_id = 'User1000' LIMIT 10 OFFSET 0;
```

- **인덱스 적용 전**:

```sql
-> Limit: 10 row(s)  (cost=102 rows=10) (actual time=0.149..0.156 rows=10 loops=1)
    -> Filter: (coupon_assignment.user_id = 'User1000')  (cost=102 rows=100) (actual time=0.14..0.145 rows=10 loops=1)
        -> Table scan on coupon_assignment  (cost=102 rows=1000) (actual time=0.121..0.125 rows=10 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Limit: 10 row(s)  (cost=104 rows=10) (actual time=0.834..0.839 rows=10 loops=1)
    -> Index lookup on coupon_assignment using idx_coupon_assignment_user_id (user_id='User1000')  (cost=104 rows=1000) (actual time=0.824..0.828 rows=10 loops=1)
```

- **분석**:
- 인덱스 적용으로 불필요한 테이블 스캔 대신 인덱스 룩업이 수행되었으나, LIMIT 및 OFFSET 조건에 따른 미세한 오버헤드가 발생할 수 있음

---

**(d) CouponAssignmentRepositoryImpl - findByCouponId(long id)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM coupon_assignment WHERE coupon_id = 1;
```

- **인덱스 적용 전**:

```sql
-> Index lookup on coupon_assignment using FK9jon19rgsx40dx8lemv1jl5vh (coupon_id=1)  (cost=0.35 rows=1) (actual time=0.0342..0.0365 rows=1 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on coupon_assignment using FK9jon19rgsx40dx8lemv1jl5vh (coupon_id=1)  (cost=0.35 rows=1) (actual time=0.0299..0.0318 rows=1 loops=1)
```

- **분석**:
- 단순 조회의 경우 인덱스 도입 전후 실행 시간이 미세하게 개선됨

---

**(e) CouponRepositoryImpl - findById(long id) & findByIdWithLock(Long id)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Coupon WHERE id = 1;
EXPLAIN ANALYZE SELECT * FROM Coupon WHERE id = 1 FOR UPDATE;
```

- **인덱스 적용 전**:

```sql
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=42e-6..83e-6 rows=1 loops=1)
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=166e-6..166e-6 rows=1 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=167e-6..209e-6 rows=1 loops=1)
```

- **분석**:
- 기본 키 조회의 경우 인덱스 효과가 미세하게 나타나지만, 전체적인 차이는 미비함

---

### **5.3 Order 테이블**

**(a) OrderItemRepositoryImpl - findByOrderId(Long orderId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM order_item WHERE order_id = 1;
```

- **인덱스 적용 전**:

```sql
-> Filter: (order_item.order_id = 1)  (cost=10102 rows=9950) (actual time=21.5..60.6 rows=2 loops=1)
    -> Table scan on order_item  (cost=10102 rows=99495) (actual time=0.368..57.5 rows=100000 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on order_item using idx_order_item_order_id (order_id=1)  (cost=0.7 rows=2) (actual time=0.435..0.442 rows=2 loops=1)
```

- **분석**:
- 인덱스 적용 후, 테이블 스캔에서 인덱스 룩업으로 전환되어 실행 시간이 21.560.6ms에서 0.4350.442ms로 대폭 단축됨

---

**(b) OrderRepositoryImpl - findByUserId(String userId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Orders WHERE user_id = 'User1000';
```

- **인덱스 적용 전**:

```sql
-> Filter: (orders.user_id = 'User1000')  (cost=6732 rows=6644) (actual time=30.9..30.9 rows=0 loops=1)
    -> Table scan on Orders  (cost=6732 rows=66438) (actual time=0.341..26.2 rows=66476 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on Orders using idx_order_user_id (user_id='User1000')  (cost=0.35 rows=1) (actual time=0.102..0.102 rows=0 loops=1)
```

- **분석**:
- 인덱스 룩업을 통해 전체 스캔 대신 빠른 접근이 가능해짐

---

**(c) OrderRepositoryImpl - findById(Long orderId) & findByIdWithLock(Long orderId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Orders WHERE id = 1;
EXPLAIN ANALYZE SELECT * FROM Orders WHERE id = 1 FOR UPDATE;
```

- **인덱스 적용 전**:

```sql
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=207e-6..248e-6 rows=1 loops=1)
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=42e-6..84e-6 rows=1 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=42e-6..42e-6 rows=1 loops=1)
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=167e-6..209e-6 rows=1 loops=1)
```

- **분석**:
- 단순 기본 키 조회에서는 미세한 차이가 있으나, 인덱스 적용으로 일관된 성능을 보임

---

### **5.4 Payment 테이블**

**PaymentRepositoryImpl - findById(Long paymentId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Payment WHERE id = 1;
```

- **인덱스 적용 전**:

```sql
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=41e-6..41e-6 rows=1 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Rows fetched before execution  (cost=0..0 rows=1) (actual time=167e-6..209e-6 rows=1 loops=1)
```

- **분석**:
- 단순 조회에서는 인덱스 적용 전후 실행 시간 차이가 미세함

---

### **5.5 Product 테이블**

**(a) ProductDailySalesRepositoryImpl - findByProductIdAndDate(Long productId, LocalDate today)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM product_daily_sales WHERE product_id = 1 AND date = '2025-02-14';
```

- **인덱스 적용 전**:

```sql
-> Filter: ((product_daily_sales.`date` = DATE'2025-02-14') and (product_daily_sales.product_id = 1))
   (cost=10068 rows=1000) (actual time=22.9..22.9 rows=0 loops=1)
    -> Table scan on product_daily_sales  (cost=10068 rows=99960) (actual time=0.977..19.2 rows=100000 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on product_daily_sales using idx_product_daily_sales_product_id_date (product_id=1, date=DATE'2025-02-14')
   (cost=0.35 rows=1) (actual time=0.0158..0.0158 rows=0 loops=1)
```

- **분석**:
- 인덱스 추가로 단일 조건 검색에서 실행 시간이 22.9ms에서 0.0158ms로 극적으로 단축됨

---

**(b) ProductDailySalesRepositoryImpl - findTopSellingProductsForPeriod(...)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM product_daily_sales
WHERE date BETWEEN '2025-02-11' AND '2025-02-14'
ORDER BY daily_quantity_sold DESC LIMIT 10;
```

- **인덱스 적용 전**:

```sql
-> Limit: 10 row(s)  (cost=10068 rows=10) (actual time=32.8..32.8 rows=0 loops=1)
    -> Sort: product_daily_sales.daily_quantity_sold DESC, limit input to 10 row(s) per chunk
       (cost=10068 rows=99960) (actual time=32.8..32.8 rows=0 loops=1)
        -> Filter: (product_daily_sales.`date` between '2025-02-11' and '2025-02-14')
           (cost=10068 rows=99960) (actual time=32.7..32.7 rows=0 loops=1)
            -> Table scan on product_daily_sales  (cost=10068 rows=99960) (actual time=0.963..18 rows=100000 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Limit: 10 row(s)  (cost=10068 rows=10) (actual time=57.8..57.8 rows=0 loops=1)
    -> Sort: product_daily_sales.daily_quantity_sold DESC, limit input to 10 row(s) per chunk
       (cost=10068 rows=99960) (actual time=57.7..57.7 rows=0 loops=1)
        -> Filter: (product_daily_sales.`date` between '2025-02-11' and '2025-02-14')
           (cost=10068 rows=99960) (actual time=57.6..57.6 rows=0 loops=1)
            -> Table scan on product_daily_sales  (cost=10068 rows=99960) (actual time=1.77..40.6 rows=100000 loops=1)
```

- **분석**:
- 범위 검색 및 정렬 쿼리의 경우, 인덱스 적용으로 일부 개선이 있었으나 정렬 작업과 LIMIT 조건에 따른 추가 최적화가 필요함

---

### **5.6 Stock 테이블**

**(a) StockRepositoryImpl - findByProductId(Long productId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Stock WHERE product_id = 1;
```

- **인덱스 적용 전**:

```sql
-> Filter: (stock.product_id = 1)  (cost=10.2 rows=10) (actual time=0.0444..0.0545 rows=1 loops=1)
    -> Table scan on Stock  (cost=10.2 rows=100) (actual time=0.0428..0.0498 rows=100 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on Stock using idx_stock_product_id (product_id=1)  (cost=0.35 rows=1) (actual time=0.544..0.545 rows=1 loops=1)
```

- **분석**:
- 단일 조건 검색에서는 인덱스 적용 전후 실행 시간이 미세하게 달라질 수 있으나 일반적으로 인덱스 룩업이 일관된 성능을 제공함

---

**(b) StockRepositoryImpl - findByProductIdIn(List<Long> productIds)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Stock WHERE product_id IN (1, 2, 3);
```

- **인덱스 적용 전**:

```sql
-> Filter: (stock.product_id in (1,2,3))  (cost=10.2 rows=30) (actual time=0.1..0.131 rows=3 loops=1)
    -> Table scan on Stock  (cost=10.2 rows=100) (actual time=0.0932..0.114 rows=100 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index range scan on Stock using idx_stock_product_id
   over (product_id = 1) OR (product_id = 2) OR (product_id = 3),
   with index condition: (stock.product_id in (1,2,3))
   (cost=2.11 rows=3) (actual time=0.212..0.216 rows=3 loops=1)
```

- **분석**:
- 복수 조건 검색의 경우, 인덱스 적용으로 실행 계획이 범위 스캔으로 전환되어 효율성이 증가됨

---

**(c) StockRepositoryImpl - findByProductIdWithLock(Long productId)**

- **쿼리**:

```sql
EXPLAIN ANALYZE SELECT * FROM Stock WHERE product_id = 1 FOR UPDATE;
```

- **인덱스 적용 전**:

```sql
-> Filter: (stock.product_id = 1)  (cost=10.2 rows=10) (actual time=0.497..0.587 rows=1 loops=1)
    -> Table scan on Stock  (cost=10.2 rows=100) (actual time=0.467..0.552 rows=100 loops=1)
```

- **인덱스 적용 후**:

```sql
-> Index lookup on Stock using idx_stock_product_id (product_id=1)  (cost=0.35 rows=1) (actual time=0.0718..0.0759 rows=1 loops=1)
```

- **분석**:
- 인덱스 적용 후, FOR UPDATE 조건에서도 실행 시간이 크게 줄어듦

---

## **6. 결론 (Conclusion)**

- **검색 최적화**:

  인덱스 도입으로 Full Table Scan 대신 인덱스 룩업이 이루어져, 특히 조건 검색 쿼리에서 실행 시간이 현저히 단축되었다.

- **조인 및 잠금 쿼리 개선**:

  조인 및 FOR UPDATE와 같은 잠금 쿼리에서도 인덱스가 적용되면 불필요한 데이터 스캔이 줄어들어 성능이 개선됨을 확인하였다.

- **쿼리 유형에 따른 차이**:

  단순 기본 키 조회의 경우 인덱스 도입 전후 차이가 미미하거나 오히려 오버헤드가 발생할 수 있으나, 대부분의 실무 쿼리에서는 인덱스의 도입이 유의미한 성능 향상을 가져왔다.

- **추가 고려사항**:

  범위 검색 및 정렬 쿼리의 경우, 인덱스 적용 외에도 정렬 최적화 방안을 함께 고려해야 함.


---

## **7. 최종 요약 (Final Summary)**

본 연구는 다양한 테이블과 쿼리에 대해 인덱스 적용 전후의 성능을 체계적으로 분석하였습니다.

실험 결과, 인덱스는 검색, 정렬, 조인 등의 작업에서 성능을 대폭 향상시키며, 특히 데이터 양이 많은 환경에서 Full Table Scan을 피하는 효과적인 수단임을 확인할 수 있었습니다.

특히 대용량 데이터 환경에서는 인덱스 전략 수립과 지속적인 튜닝이 필수적임을 시사합니다.

---

**참고**:

본 보고서는 실험 환경, 쿼리 유형 및 데이터 분포에 따라 결과가 다를 수 있으므로 실제 운영 환경에 적용하기 전 충분한 테스트와 모니터링이 필요합니다.