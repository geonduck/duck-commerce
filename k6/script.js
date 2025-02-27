import http from 'k6/http';
import { check, sleep } from 'k6';

// 🛠 환경 변수 설정 (API 엔드포인트)
const BASE_URL = 'http://host.docker.internal:8080'; // 로컬 개발 환경
const COUPON_ENDPOINT = '/api/v1/coupons/assign';


export const options = {
    stages: [
        { duration: '1m', target: 100 },   // 1분 동안 100명 증가
        { duration: '2m', target: 500 },   // 2분 동안 500명 유지
        { duration: '3m', target: 1000 },  // 3분 동안 1000명 증가
        { duration: '2m', target: 1000 },  // 2분 동안 유지
        { duration: '1m', target: 500 },   // 1분 동안 감소
        { duration: '30s', target: 0 },    // 30초 동안 부하 감소
    ],
};

export default function () {
    const headers = { 'Content-Type': 'application/json' };

    const payload = JSON.stringify({
        user_id: `user${__VU}${__ITER}`, // 가상의 사용자 ID 생성
        coupon_id: 123,
    });

    const params = {
        headers: headers,
        timeout: '60s', // 타임아웃 설정
    };

    const res = http.post(`${BASE_URL}${COUPON_ENDPOINT}`, payload, params);

    const slowThreshold = 5000; // 5초 이상 걸리는 요청만 로깅
    if (res.timings.duration > slowThreshold) {
        console.warn(`⚠️ SLOW REQUEST: Response time: ${res.timings.duration} ms, Status: ${res.status}, URL: ${res.url}`);
    }
    if (res.status !== 200) {
        console.error(`❌ ERROR: Response time: ${res.timings.duration} ms, Status: ${res.status}, URL: ${res.url}, Body: ${JSON.stringify(res.body)}`);
    }

    // 요청 성공 여부 확인
    let success = check(res, {
        'is status 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}