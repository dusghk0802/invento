package com.invento.domain.production.service;

import com.invento.domain.admin.repository.AdminRepository;
import com.invento.domain.location.entity.Location;
import com.invento.domain.location.entity.LocationType;
import com.invento.domain.location.repository.LocationRepository;
import com.invento.domain.product.repository.ProductRepository;
import com.invento.domain.production.dto.ProductionDto.*;
import com.invento.domain.production.entity.ProductionOrder;
import com.invento.domain.production.entity.ProductionOrderItem;
import com.invento.domain.production.entity.ProductionReceipt;
import com.invento.domain.production.repository.ProductionOrderRepository;
import com.invento.domain.production.repository.ProductionReceiptRepository;
import com.invento.domain.stock.entity.StockMovementType;
import com.invento.domain.stock.service.StockService;
import com.invento.domain.supplier.repository.SupplierRepository;
import com.invento.global.common.AuthUtils;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.invento.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductionService {

    private final ProductionOrderRepository repo;
    private final ProductionReceiptRepository receiptRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;
    private final LocationRepository locationRepo;
    private final AdminRepository adminRepo;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<Summary> findAll(Search search) {
        boolean owner = AuthUtils.isOwner();
        return repo.search(search).stream().map(o -> Summary.of(o, owner)).toList();
    }

    @Transactional(readOnly = true)
    public Detail findOne(Long id) {
        boolean owner = AuthUtils.isOwner();
        ProductionOrder o = repo.getOrThrow(id);
        return new Detail(Summary.of(o, owner),
                o.getItems().stream().map(i -> ItemResponse.of(i, owner)).toList(),
                receiptRepo.findByOrderItemProductionOrderProductionOrderIdOrderByProductionReceiptIdDesc(id)
                        .stream().map(ReceiptResponse::from).toList());
    }

    public Detail create(Request r) {
        ProductionOrder o = ProductionOrder.builder()
                .supplier(supplierRepo.getOrThrow(r.supplierId())).orderDate(r.orderDate())
                .expectedDate(r.expectedDate()).memo(r.memo()).createdBy(adminRepo.current().getAdminId())
                .build();
        addItems(o, r.items());
        return findOne(repo.save(o).getProductionOrderId());
    }

    public Detail update(Long id, Request r) {
        ProductionOrder o = repo.getOrThrow(id);
        o.update(supplierRepo.getOrThrow(r.supplierId()), r.orderDate(), r.expectedDate(), r.memo());
        addItems(o, r.items());
        return findOne(id);
    }

    public void cancel(Long id) { repo.getOrThrow(id).cancel(); }

    // 입고: 항목별 입고·불량 기록 → 정상 수량만 창고 재고 증가(원장 PRODUCTION_IN) → 발주 상태 자동 변경
    public Detail receive(Long id, ReceiveRequest r) {
        ProductionOrder o = repo.getOrThrow(id);
        o.checkReceivable();
        Location location = locationRepo.getOrThrow(r.locationId());
        if (location.getLocationType() != LocationType.WAREHOUSE) throw new BusinessException(WAREHOUSE_ONLY);
        stockService.checkLocation(location.getLocationId()); // 창고 직원은 담당 창고만
        Long adminId = adminRepo.current().getAdminId();

        int processed = 0;
        for (ReceiveItem ri : r.items()) {
            if (ri.receivedQty() + ri.defectiveQty() == 0) continue;
            ProductionOrderItem item = o.getItems().stream()
                    .filter(i -> i.getProductionOrderItemId().equals(ri.productionOrderItemId()))
                    .findFirst().orElseThrow(() -> new BusinessException(NOT_FOUND));
            item.receive(ri.receivedQty(), ri.defectiveQty());
            ProductionReceipt receipt = receiptRepo.save(ProductionReceipt.builder()
                    .orderItem(item).location(location).receivedDate(r.receivedDate())
                    .receivedQty(ri.receivedQty()).defectiveQty(ri.defectiveQty()).receivedBy(adminId)
                    .build());
            if (ri.receivedQty() > 0) {
                stockService.change(item.getProduct().getProductId(), location.getLocationId(),
                        StockMovementType.PRODUCTION_IN, ri.receivedQty(), null,
                        "PRODUCTION_RECEIPT", receipt.getProductionReceiptId(), null);
            }
            processed++;
        }
        if (processed == 0) throw new BusinessException(INVALID_QUANTITY);
        o.refreshStatus();
        return findOne(id);
    }

    // 입고 수량 수정 (잘못 입력한 경우): 정상 수량 차이만큼 창고 재고를 다시 맞추고 원장에 정정 기록 추가
    // 이미 팔려서 창고 재고가 모자라면 수정 불가 (STOCK_NOT_ENOUGH)
    public Detail correctReceipt(Long id, Long receiptId, CorrectRequest r) {
        ProductionOrder o = repo.getOrThrow(id);
        o.checkCorrectable();
        ProductionReceipt receipt = receiptRepo.getOrThrow(receiptId);
        ProductionOrderItem item = receipt.getOrderItem();
        if (!item.getProductionOrder().getProductionOrderId().equals(id)) throw new BusinessException(NOT_FOUND);
        stockService.checkLocation(receipt.getLocation().getLocationId());

        int okDelta = r.receivedQty() - receipt.getReceivedQty();
        int defectiveDelta = r.defectiveQty() - receipt.getDefectiveQty();
        if (okDelta == 0 && defectiveDelta == 0) return findOne(id);
        item.correct(okDelta, defectiveDelta);
        receipt.correct(r.receivedQty(), r.defectiveQty());
        if (okDelta != 0) {
            stockService.change(item.getProduct().getProductId(), receipt.getLocation().getLocationId(),
                    StockMovementType.PRODUCTION_IN, okDelta, null, "PRODUCTION_RECEIPT", receiptId, "입고 수량 수정");
        }
        o.refreshStatus();
        return findOne(id);
    }

    private void addItems(ProductionOrder o, List<ItemRequest> items) {
        items.forEach(i -> o.addItem(productRepo.getOrThrow(i.productId()), i.orderedQty()));
    }
}
