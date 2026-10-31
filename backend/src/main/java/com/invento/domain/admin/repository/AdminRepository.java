package com.invento.domain.admin.repository;

import com.invento.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);

    // 담당 위치까지 같이 조회 (트랜잭션 밖에서 위치 정보를 쓸 때)
    @EntityGraph(attributePaths = "location")
    Optional<Admin> findWithLocationByLoginId(String loginId);
}