package com.invento.domain.transfer.service;

import com.invento.domain.admin.entity.Admin;
import com.invento.domain.admin.entity.AdminRole;
import com.invento.domain.admin.repository.AdminRepository;
import com.invento.domain.location.repository.LocationRepository;
import com.invento.domain.product.repository.ProductRepository;
import com.invento.domain.stock.entity.StockMovementType;
import com.invento.domain.stock.service.StockService;
import com.invento.domain.transfer.dto.TransferDto.*;
import com.invento.domain.transfer.entity.StockTransfer;
import com.invento.domain.transfer.entity.StockTransferItem;
import com.invento.domain.transfer.entity.TransferStatus;
import com.invento.domain.transfer.repository.StockTransferRepository;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.invento.global.exception.ErrorCode.*;

// 단계별 권한: 요청 = 도착지 담당(매장 직원), 출고 = 출발지 담당(창고 직원), 도착 확인 = 도착지 담당. 사장은 전부
@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private static final String REF_TYPE = "STOCK_TRANSFER";

    private final StockTransferRepository repo;
    private final ProductRepository productRepo;
    private final LocationRepository locationRepo;
    private final AdminRepository adminRepo;
    private final StockService stockService;

    // 사장은 전체, 직원은 자기 위치가 출발지나 도착지인 이동만
    @Transactional(readOnly = true)
    public List<Summary> findAll(TransferStatus status) {
        Admin me = adminRepo.current();
        var list = status == null ? repo.findAllByOrderByStockTransferIdDesc() : repo.findByStatusOrderByStockTransferIdDesc(status);
        return list.stream().filter(t -> involved(me, t)).map(Summary::from).toList();
    }

    @Transactional(readOnly = true)
    public Detail findOne(Long id) { return Detail.from(getVisible(id)); }

    public Detail request(Request r) {
        if (r.fromLocationId().equals(r.toLocationId())) throw new BusinessException(SAME_LOCATION);
        stockService.checkLocation(r.toLocationId()); // 받는 쪽(매장) 담당만 요청
        StockTransfer t = StockTransfer.builder()
                .fromLocation(locationRepo.getOrThrow(r.fromLocationId()))
                .toLocation(locationRepo.getOrThrow(r.toLocationId()))
                .requestedBy(adminRepo.current().getAdminId()).memo(r.memo())
                .build();
        r.items().forEach(i -> t.addItem(productRepo.getOrThrow(i.productId()), i.boxCount(), i.looseQty()));
        if (t.getItems().stream().anyMatch(i -> i.getQuantity() == 0)) throw new BusinessException(INVALID_QUANTITY);
        return Detail.from(repo.save(t));
    }

    // 출고: 실제 보내는 수량으로 고친 뒤 출발지 재고 차감 (판매 가능 재고가 모자라면 실패)
    // 승인: 창고 담당이 보낼 수 있는 수량으로 확정 (재고는 출고할 때 빠짐)
    public Detail approve(Long id, ShipRequest r) {
        StockTransfer t = fromSide(id, r);
        t.approve(adminRepo.current().getAdminId());
        return Detail.from(t);
    }

    // 반려: 창고 담당이 사유와 함께 거절 (예: 창고 재고 없음)
    public Detail reject(Long id, RejectRequest r) {
        StockTransfer t = repo.getOrThrow(id);
        stockService.checkLocation(t.getFromLocation().getLocationId());
        t.reject(adminRepo.current().getAdminId(), r.reason());
        return Detail.from(t);
    }

    public Detail ship(Long id, ShipRequest r) {
        StockTransfer t = fromSide(id, r);
        t.ship(adminRepo.current().getAdminId());
        move(t, StockMovementType.TRANSFER_OUT, t.getFromLocation().getLocationId(), -1);
        return Detail.from(t);
    }

    // 도착 확인: 보낸 수량 그대로 도착지 재고 증가. 실제와 다르면 재고 조정(이동 수량 차이)으로 처리
    public Detail receive(Long id) {
        StockTransfer t = repo.getOrThrow(id);
        stockService.checkLocation(t.getToLocation().getLocationId());
        t.receive(adminRepo.current().getAdminId());
        move(t, StockMovementType.TRANSFER_IN, t.getToLocation().getLocationId(), 1);
        return Detail.from(t);
    }

    // 취소: 요청한 매장 쪽(또는 사장)만, 출고 전까지
    public void cancel(Long id) {
        StockTransfer t = repo.getOrThrow(id);
        stockService.checkLocation(t.getToLocation().getLocationId());
        t.cancel();
    }

    // 창고(출발지) 담당 확인 + 보낼 수량 수정
    private StockTransfer fromSide(Long id, ShipRequest r) {
        StockTransfer t = repo.getOrThrow(id);
        stockService.checkLocation(t.getFromLocation().getLocationId());
        if (r != null && r.items() != null) {
            for (ShipItem si : r.items()) itemOf(t, si.stockTransferItemId()).changeQty(si.boxCount(), si.looseQty());
        }
        if (t.getTotalQty() == 0) throw new BusinessException(INVALID_QUANTITY);
        return t;
    }

    private void move(StockTransfer t, StockMovementType type, Long locationId, int sign) {
        t.getItems().stream().filter(i -> i.getQuantity() > 0).forEach(i ->
                stockService.change(i.getProduct().getProductId(), locationId, type, sign * i.getQuantity(),
                        null, REF_TYPE, t.getStockTransferId(), null));
    }

    private StockTransferItem itemOf(StockTransfer t, Long itemId) {
        return t.getItems().stream().filter(i -> i.getStockTransferItemId().equals(itemId))
                .findFirst().orElseThrow(() -> new BusinessException(NOT_FOUND));
    }

    private StockTransfer getVisible(Long id) {
        StockTransfer t = repo.getOrThrow(id);
        if (!involved(adminRepo.current(), t)) throw new BusinessException(FORBIDDEN_LOCATION);
        return t;
    }

    private boolean involved(Admin me, StockTransfer t) {
        if (me.getRole() == AdminRole.OWNER) return true;
        if (me.getLocation() == null) return false;
        Long mine = me.getLocation().getLocationId();
        return mine.equals(t.getFromLocation().getLocationId()) || mine.equals(t.getToLocation().getLocationId());
    }
}
