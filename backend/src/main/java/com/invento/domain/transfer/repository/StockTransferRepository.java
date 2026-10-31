package com.invento.domain.transfer.repository;

import com.invento.domain.transfer.entity.StockTransfer;
import com.invento.domain.transfer.entity.TransferStatus;
import com.invento.global.common.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface StockTransferRepository extends BaseRepository<StockTransfer> {

    @EntityGraph(attributePaths = {"fromLocation", "toLocation"})
    List<StockTransfer> findAllByOrderByStockTransferIdDesc();

    @EntityGraph(attributePaths = {"fromLocation", "toLocation"})
    List<StockTransfer> findByStatusOrderByStockTransferIdDesc(TransferStatus status);
}
