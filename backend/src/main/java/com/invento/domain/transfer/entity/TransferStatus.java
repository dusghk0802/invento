package com.invento.domain.transfer.entity;

// 요청(매장) → 승인(창고, 수량 확정) / 반려(사유) → 출고(창고 재고 차감) → 도착(매장 재고 증가)
// 매장은 출고 전(요청·승인 상태)까지 취소 가능
public enum TransferStatus {
    REQUESTED, APPROVED, REJECTED, SHIPPED, RECEIVED, CANCELLED
}
