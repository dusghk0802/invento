package com.invento.domain.stock.entity;

import com.invento.domain.packaging.entity.Packaging;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

// 위치별 포장재 재고. 주문 예약 없이 사용할 때 바로 차감
@Entity @Table(name = "packaging_stocks", uniqueConstraints = @UniqueConstraint(columnNames = {"packaging_id", "location_id"}))
@Getter @SuperBuilder @NoArgsConstructor(access = PROTECTED)
public class PackagingStock extends LocationStock {

    @Id @GeneratedValue(strategy = IDENTITY) private Long packagingStockId;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "packaging_id") private Packaging packaging;
}
