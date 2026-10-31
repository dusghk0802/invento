package com.invento.domain.stock.entity;

// 재고 조정 사유: 실사 차이, 파손, 불량(보관 중 발견), 분실, 이동 수량 차이, 기타
public enum AdjustReason {
    COUNT_DIFF, DAMAGE, DEFECT, LOSS, TRANSFER_DIFF, ETC
}
