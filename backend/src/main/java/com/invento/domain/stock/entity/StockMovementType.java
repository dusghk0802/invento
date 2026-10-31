package com.invento.domain.stock.entity;

public enum StockMovementType {
    PRODUCTION_IN,  // 공장 입고 (+), 입고 수량 수정 시 차이만큼 (+/-)
    PURCHASE_IN,    // 구매 입고 (+) 포장재처럼 직접 사 온 것
    TRANSFER_OUT,   // 재고 이동 출발 (-)
    TRANSFER_IN,    // 재고 이동 도착 (+)
    SALE_OUT,       // 판매 출고 (-)
    GIFT_OUT,       // 증정 출고 (-) 판매량·매출에 포함 안 됨
    RETURN_IN,      // 반품·환불 재입고 (+)
    DISPOSAL,       // 불량 폐기 (-)
    PACKAGING_OUT,  // 포장재 사용 (-)
    ADJUST          // 재고 조정 (+/-)
}
