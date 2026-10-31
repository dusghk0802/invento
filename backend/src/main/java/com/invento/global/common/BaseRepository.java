package com.invento.global.common;

import com.invento.global.exception.BusinessException;
import com.invento.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

// 모든 Repository 공통: ID로 찾고 없으면 404
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long> {

    default T getOrThrow(Long id) { return findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND)); }
}
