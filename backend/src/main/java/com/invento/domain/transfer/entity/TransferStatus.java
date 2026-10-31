package com.invento.domain.transfer.entity;

// 요청(매장) → 출고(창고, 출발지 재고 차감) → 도착(매장, 도착지 재고 증가), 요청 상태에서만 취소
public enum TransferStatus {
    REQUESTED, SHIPPED, RECEIVED, CANCELLED
}
