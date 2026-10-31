package com.invento.domain.stock.entity;

import com.invento.domain.location.entity.Location;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PRIVATE;

// 재고 원장: 모든 재고 변동 기록. 추가만 하고 수정·삭제하지 않음
@Entity @EntityListeners(AuditingEntityListener.class)
@Table(name = "stock_movements", indexes = {@Index(columnList = "item_type, item_id, created_at"),
        @Index(columnList = "location_id, created_at"), @Index(columnList = "ref_type, ref_id")})
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class StockMovement {

    @Id @GeneratedValue(strategy = IDENTITY) private Long stockMovementId;
    @ManyToOne(fetch = LAZY) @JoinColumn(name = "location_id") private Location location; // 재고가 바뀐 위치 (상품·포장재 모두)
    @Enumerated(STRING) @Column(nullable = false) private StockItemType itemType;
    @Column(nullable = false) private Long itemId; // itemType에 따라 상품 ID / 포장재 ID
    @Enumerated(STRING) @Column(nullable = false, columnDefinition = "varchar(30)") private StockMovementType movementType;
    @Column(nullable = false) private int quantity; // + 입고·도착·복원, - 출고
    @Enumerated(STRING) @Column(columnDefinition = "varchar(30)") private AdjustReason adjustReason; // ADJUST일 때만
    @Column(length = 30) private String refType; // 변동 원인 (PRODUCTION_RECEIPT, ORDER_ITEM 등), 수동이면 null
    private Long refId;
    @Column(length = 500) private String memo;
    private Long createdBy; // 처리한 관리자 ID
    @CreatedDate @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
}
