package com.invento.domain.production.entity;

// 발주(공장 확인 대기) → 공장 확인(확정 납기일) → 일부 입고 → 입고 완료 (입고 수량에 따라 자동 변경), 취소
public enum ProductionStatus {
    ORDERED, CONFIRMED, PARTIAL, RECEIVED, CANCELLED
}
