package kr.hhplus.be.server.domain.balance;

/**
 * 변화 상태
 * - CHARGE : 충전
 * - USE : 사용
 * - CANCEL : 취소
 * - FAIL : 실패
 */
public enum BalanceStatus {
    CHARGE, USE, CANCEL, FAIL
}
