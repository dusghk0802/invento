package com.invento.domain.order.entity;

// 온라인: 결제 대기(재고 예약) → 결제완료 → 포장중 → 출고(실제 차감) → 배송완료, 출고 전 취소
// 매장: 저장 즉시 판매완료(COMPLETED)
public enum OrderStatus { PENDING_PAYMENT, PAID, PACKING, SHIPPED, DELIVERED, COMPLETED, CANCELLED }
