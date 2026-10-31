package com.invento.domain.admin.repository;

import com.invento.domain.admin.entity.Admin;
import com.invento.global.common.AuthUtils;
import com.invento.global.exception.BusinessException;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static com.invento.global.exception.ErrorCode.NOT_FOUND;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);

    @EntityGraph(attributePaths = "location")
    Optional<Admin> findWithLocationByLoginId(String loginId);

    // 지금 로그인한 관리자 (담당 위치까지 같이 조회)
    default Admin current() {
        return findWithLocationByLoginId(AuthUtils.loginId()).orElseThrow(() -> new BusinessException(NOT_FOUND));
    }
}