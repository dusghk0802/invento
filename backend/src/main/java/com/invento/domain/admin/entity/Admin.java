package com.invento.domain.admin.entity;

import com.invento.domain.location.entity.Location;
import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "admins")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Admin extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String adminName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;

    // 담당 위치: 매장 직원=매장, 창고 직원=창고, 사장·출고 직원=NULL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Setter
    private LocalDateTime lastLoginAt;
}