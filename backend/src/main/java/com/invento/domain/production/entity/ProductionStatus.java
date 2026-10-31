package com.invento.domain.production.entity;

// 발주 → 일부 입고 → 입고 완료 (입고 수량에 따라 자동 변경), 취소
public enum ProductionStatus {
    ORDERED, PARTIAL, RECEIVED, CANCELLED
}
