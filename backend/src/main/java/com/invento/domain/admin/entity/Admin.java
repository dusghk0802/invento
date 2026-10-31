package com.invento.domain.admin.entity;

import com.invento.domain.location.entity.Location;
import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PRIVATE;

@Entity @Table(name = "admins")
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class Admin extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY) private Long adminId;
    @Column(nullable = false, unique = true, length = 50) private String loginId;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false, length = 50) private String adminName;
    @Enumerated(STRING) @Column(nullable = false) private AdminRole role;

    // 담당 위치: 매장 직원=매장, 창고 직원=창고, 사장·출고 직원=NULL
    @ManyToOne(fetch = LAZY) @JoinColumn(name = "location_id") private Location location;
    @Builder.Default @Column(nullable = false) private boolean active = true;
    @Setter private LocalDateTime lastLoginAt;
}