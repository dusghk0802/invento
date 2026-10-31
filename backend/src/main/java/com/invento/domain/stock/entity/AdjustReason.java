package com.invento.domain.stock.entity;

// 재고 조정 사유: 실사 차이, 파손, 분실, 이동 수량 차이, 기타
public enum AdjustReason {
    COUNT_DIFF, DAMAGE, LOSS, TRANSFER_DIFF, ETC
}
