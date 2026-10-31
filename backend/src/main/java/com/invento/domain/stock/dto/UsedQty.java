package com.invento.domain.stock.dto;

// 품목·위치별 사용 수량 합계 (원장 집계 결과)
public record UsedQty(Long itemId, Long locationId, Long qty) {}
