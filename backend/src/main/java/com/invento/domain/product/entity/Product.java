package com.invento.domain.product.entity;

import com.invento.domain.producttype.entity.ProductType;
import com.invento.domain.series.entity.Series;
import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 상품 정보만 저장. 재고 수량은 위치별로 ProductStock에 있음
@Getter
@Entity
@Table(name = "products")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemKind itemKind;

    @Column(nullable = false, unique = true, length = 50)
    private String productCode; // 삭제된 상품의 번호도 재사용 불가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(length = 1000)
    private String imageUrl;

    @Column(columnDefinition = "text")
    private String description; // 쇼핑몰 상세 설명

    @Column(nullable = false)
    private boolean shopVisible; // 쇼핑몰 노출

    @Column(nullable = false)
    private long costPrice; // 원가 (사장만 조회)

    @Column(nullable = false)
    private long sellingPrice;

    private LocalDate releaseDate;

    private Integer boxQty; // 박스 입수량 (매장 출고는 박스 단위). 낱개만 다루면 null

    private LocalDateTime deletedAt; // Soft Delete

    public void update(ItemKind itemKind, String productCode, Series series, ProductType productType, String productName,
                       String imageUrl, String description, boolean shopVisible, long costPrice, long sellingPrice,
                       LocalDate releaseDate, Integer boxQty) {
        this.itemKind = itemKind;
        this.productCode = productCode;
        this.series = series;
        this.productType = productType;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.description = description;
        this.shopVisible = shopVisible;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.releaseDate = releaseDate;
        this.boxQty = boxQty;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
