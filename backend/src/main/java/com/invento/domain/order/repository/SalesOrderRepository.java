package com.invento.domain.order.repository;

import com.invento.domain.order.entity.SalesChannel;
import com.invento.domain.order.entity.SalesOrder;
import com.invento.global.common.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderRepository extends BaseRepository<SalesOrder> {

    @EntityGraph(attributePaths = "location")
    List<SalesOrder> findByChannelAndOrderDateBetweenOrderByOrderIdDesc(SalesChannel channel, LocalDateTime from, LocalDateTime to);

    long countByChannelAndOrderDateBetween(SalesChannel channel, LocalDateTime from, LocalDateTime to);
}
