package com.invento.domain.stock.service;

import com.invento.domain.admin.entity.Admin;
import com.invento.domain.admin.repository.AdminRepository;
import com.invento.domain.location.entity.Location;
import com.invento.domain.location.repository.LocationRepository;
import com.invento.domain.packaging.repository.PackagingRepository;
import com.invento.domain.product.repository.ProductRepository;
import com.invento.domain.stock.dto.StockDto.*;
import com.invento.domain.stock.entity.*;
import com.invento.domain.stock.repository.PackagingStockRepository;
import com.invento.domain.stock.repository.ProductStockRepository;
import com.invento.domain.stock.repository.StockMovementRepository;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.groupingBy;

import static com.invento.domain.location.entity.LocationType.WAREHOUSE;
import static com.invento.domain.stock.entity.StockMovementType.ADJUST;
import static com.invento.domain.stock.entity.StockMovementType.PURCHASE_IN;
import static com.invento.global.exception.ErrorCode.FORBIDDEN_LOCATION;
import static com.invento.global.exception.ErrorCode.INVALID_QUANTITY;

// 재고 변경의 유일한 입구. 입고·판매·이동·조정·포장재 사용 모두 change()/changePackaging()을 거쳐
// 수량 변경과 원장 기록이 같이 처리됨
@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final ProductStockRepository stockRepo;
    private final PackagingStockRepository packagingStockRepo;
    private final StockMovementRepository movementRepo;
    private final ProductRepository productRepo;
    private final PackagingRepository packagingRepo;
    private final LocationRepository locationRepo;
    private final AdminRepository adminRepo;

    public ProductStock change(Long productId, Long locationId, StockMovementType type, int quantity,
                               AdjustReason reason, String refType, Long refId, String memo) {
        return apply(productStock(productId, locationId), StockItemType.PRODUCT, productId, type, quantity, reason, refType, refId, memo);
    }

    public PackagingStock changePackaging(Long packagingId, Long locationId, StockMovementType type, int quantity,
                                          AdjustReason reason, String refType, Long refId, String memo) {
        return apply(packagingStock(packagingId, locationId), StockItemType.PACKAGING, packagingId, type, quantity, reason, refType, refId, memo);
    }

    public void adjust(AdjustRequest r) {
        checkLocation(r.locationId());
        change(r.productId(), r.locationId(), ADJUST, r.quantity(), r.reason(), null, null, r.memo());
    }

    public void adjustPackaging(PackagingAdjustRequest r) {
        checkLocation(r.locationId());
        changePackaging(r.packagingId(), r.locationId(), ADJUST, r.quantity(), r.reason(), null, null, r.memo());
    }

    public void purchasePackaging(PackagingPurchaseRequest r) {
        checkLocation(r.locationId());
        changePackaging(r.packagingId(), r.locationId(), PURCHASE_IN, r.quantity(), null, null, null, r.memo());
    }

    public void changeMinimum(MinimumRequest r) {
        checkLocation(r.locationId());
        productStock(r.productId(), r.locationId()).changeMinimum(r.minimumQty());
    }

    public void changePackagingMinimum(PackagingMinimumRequest r) {
        checkLocation(r.locationId());
        packagingStock(r.packagingId(), r.locationId()).changeMinimum(r.minimumQty());
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> movements(StockItemType itemType, Long itemId) {
        var visible = visibleLocations();
        return movementRepo.findTop200ByItemTypeAndItemIdOrderByStockMovementIdDesc(itemType, itemId)
                .stream().filter(m -> visible.test(m.getLocation())).map(MovementResponse::from).toList();
    }

    // 목록 화면용: 권한별로 볼 수 있는 위치의 재고만 골라서 상품(포장재) ID별로 묶기 (위치 순서대로)
    @Transactional(readOnly = true)
    public <S extends LocationStock> Map<Long, List<S>> groupVisible(List<S> stocks, Function<S, Long> itemId) {
        var visible = visibleLocations();
        return stocks.stream().filter(s -> visible.test(s.getLocation()))
                .sorted(Comparator.comparing(s -> s.getLocation().getLocationId())).collect(groupingBy(itemId));
    }

    @Transactional(readOnly = true)
    public Predicate<Location> visibleLocations() { return scope(false); }

    public void checkLocation(Long locationId) {
        if (!scope(true).test(locationRepo.getOrThrow(locationId))) throw new BusinessException(FORBIDDEN_LOCATION);
    }

    // 권한별 위치 범위. 조회: 사장=전체, 출고 직원=창고, 매장 직원=담당 매장+창고(온라인 재고), 창고 직원=담당 창고
    // 수정(조정·입고·이동): 같은데 매장 직원은 담당 매장만
    private Predicate<Location> scope(boolean write) {
        Admin admin = adminRepo.current();
        Long mine = admin.getLocation() == null ? null : admin.getLocation().getLocationId();
        Predicate<Location> own = l -> l.getLocationId().equals(mine);
        return switch (admin.getRole()) {
            case OWNER -> l -> true;
            case FULFILLMENT -> l -> l.getLocationType() == WAREHOUSE;
            case STORE -> write ? own : own.or(l -> l.getLocationType() == WAREHOUSE);
            case WAREHOUSE -> own;
        };
    }

    // 수량 변경(0 미만, 상품은 예약 수량 미만이면 STOCK_NOT_ENOUGH) + 원장 기록
    private <S extends LocationStock> S apply(S stock, StockItemType itemType, Long itemId, StockMovementType type,
                                              int quantity, AdjustReason reason, String refType, Long refId, String memo) {
        if (quantity == 0) throw new BusinessException(INVALID_QUANTITY);
        stock.changeOnHand(quantity);
        movementRepo.save(StockMovement.builder()
                .location(stock.getLocation()).itemType(itemType).itemId(itemId).movementType(type).quantity(quantity)
                .adjustReason(reason).refType(refType).refId(refId).memo(memo).createdBy(adminRepo.current().getAdminId())
                .build());
        return stock;
    }

    // 재고 행이 없으면 0개로 생성 (상품·포장재 등록 시 미리 만들지 않음)
    private ProductStock productStock(Long productId, Long locationId) {
        return stockRepo.findByProductProductIdAndLocationLocationId(productId, locationId)
                .orElseGet(() -> stockRepo.save(ProductStock.builder()
                        .product(productRepo.getOrThrow(productId)).location(locationRepo.getOrThrow(locationId)).build()));
    }

    private PackagingStock packagingStock(Long packagingId, Long locationId) {
        return packagingStockRepo.findByPackagingPackagingIdAndLocationLocationId(packagingId, locationId)
                .orElseGet(() -> packagingStockRepo.save(PackagingStock.builder()
                        .packaging(packagingRepo.getOrThrow(packagingId)).location(locationRepo.getOrThrow(locationId)).build()));
    }
}
