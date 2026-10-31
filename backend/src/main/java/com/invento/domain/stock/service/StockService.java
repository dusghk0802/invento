package com.invento.domain.stock.service;

import com.invento.domain.admin.entity.Admin;
import com.invento.domain.admin.repository.AdminRepository;
import com.invento.domain.location.entity.Location;
import com.invento.domain.location.entity.LocationType;
import com.invento.domain.location.repository.LocationRepository;
import com.invento.domain.packaging.dto.PackagingDto;
import com.invento.domain.packaging.repository.PackagingRepository;
import com.invento.domain.product.dto.ProductDto.StockResponse;
import com.invento.domain.product.repository.ProductRepository;
import com.invento.domain.stock.dto.StockDto.AdjustRequest;
import com.invento.domain.stock.dto.StockDto.MinimumRequest;
import com.invento.domain.stock.dto.StockDto.MovementResponse;
import com.invento.domain.stock.dto.StockDto.PackagingAdjustRequest;
import com.invento.domain.stock.dto.StockDto.PackagingMinimumRequest;
import com.invento.domain.stock.entity.*;
import com.invento.domain.stock.repository.PackagingStockRepository;
import com.invento.domain.stock.repository.ProductStockRepository;
import com.invento.domain.stock.repository.StockMovementRepository;
import com.invento.global.common.AuthUtils;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
        if (quantity == 0) throw new BusinessException(INVALID_QUANTITY);
        ProductStock stock = getOrCreate(productId, locationId);
        stock.changeOnHand(quantity); // 0 미만이 되면 STOCK_NOT_ENOUGH
        record(stock.getLocation(), StockItemType.PRODUCT, productId, type, quantity, reason, refType, refId, memo);
        return stock;
    }

    public PackagingStock changePackaging(Long packagingId, Long locationId, StockMovementType type, int quantity,
                                          AdjustReason reason, String refType, Long refId, String memo) {
        if (quantity == 0) throw new BusinessException(INVALID_QUANTITY);
        PackagingStock stock = getOrCreatePackaging(packagingId, locationId);
        stock.changeOnHand(quantity);
        record(stock.getLocation(), StockItemType.PACKAGING, packagingId, type, quantity, reason, refType, refId, memo);
        return stock;
    }

    public StockResponse adjust(AdjustRequest r) {
        checkLocation(r.locationId());
        return StockResponse.from(change(r.productId(), r.locationId(), StockMovementType.ADJUST, r.quantity(),
                r.reason(), null, null, r.memo()));
    }

    public StockResponse changeMinimum(MinimumRequest r) {
        checkLocation(r.locationId());
        ProductStock stock = getOrCreate(r.productId(), r.locationId());
        stock.changeMinimum(r.minimumQty());
        return StockResponse.from(stock);
    }

    public PackagingDto.StockResponse adjustPackaging(PackagingAdjustRequest r) {
        checkLocation(r.locationId());
        return PackagingDto.StockResponse.from(changePackaging(r.packagingId(), r.locationId(),
                StockMovementType.ADJUST, r.quantity(), r.reason(), null, null, r.memo()));
    }

    public PackagingDto.StockResponse changePackagingMinimum(PackagingMinimumRequest r) {
        checkLocation(r.locationId());
        PackagingStock stock = getOrCreatePackaging(r.packagingId(), r.locationId());
        stock.changeMinimum(r.minimumQty());
        return PackagingDto.StockResponse.from(stock);
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> movements(StockItemType itemType, Long itemId) {
        var visible = visibleLocations();
        return movementRepo.findTop200ByItemTypeAndItemIdOrderByStockMovementIdDesc(itemType, itemId)
                .stream().filter(m -> visible.test(m.getLocation())).map(MovementResponse::from).toList();
    }

    // 권한별로 볼 수 있는 재고 위치: 사장=전체, 출고 직원=창고, 매장 직원=담당 매장+창고(온라인 재고), 창고 직원=담당 창고
    @Transactional(readOnly = true)
    public Predicate<Location> visibleLocations() {
        Admin admin = currentAdmin().orElse(null);
        if (admin == null) return l -> false;
        Long mine = admin.getLocation() == null ? null : admin.getLocation().getLocationId();
        return switch (admin.getRole()) {
            case OWNER -> l -> true;
            case FULFILLMENT -> l -> l.getLocationType() == LocationType.WAREHOUSE;
            case STORE -> l -> l.getLocationType() == LocationType.WAREHOUSE || l.getLocationId().equals(mine);
            case WAREHOUSE -> l -> l.getLocationId().equals(mine);
        };
    }

    private void record(Location location, StockItemType itemType, Long itemId, StockMovementType type, int quantity,
                        AdjustReason reason, String refType, Long refId, String memo) {
        movementRepo.save(StockMovement.builder()
                .location(location).itemType(itemType).itemId(itemId)
                .movementType(type).quantity(quantity).adjustReason(reason)
                .refType(refType).refId(refId).memo(memo)
                .createdBy(currentAdmin().map(Admin::getAdminId).orElse(null))
                .build());
    }

    // 재고 행이 없으면 0개로 생성 (상품·포장재 등록 시 미리 만들지 않음)
    private ProductStock getOrCreate(Long productId, Long locationId) {
        return stockRepo.findByProductProductIdAndLocationLocationId(productId, locationId)
                .orElseGet(() -> stockRepo.save(ProductStock.builder()
                        .product(productRepo.getOrThrow(productId))
                        .location(locationRepo.getOrThrow(locationId))
                        .build()));
    }

    private PackagingStock getOrCreatePackaging(Long packagingId, Long locationId) {
        return packagingStockRepo.findByPackagingPackagingIdAndLocationLocationId(packagingId, locationId)
                .orElseGet(() -> packagingStockRepo.save(PackagingStock.builder()
                        .packaging(packagingRepo.getOrThrow(packagingId))
                        .location(locationRepo.getOrThrow(locationId))
                        .build()));
    }

    // 직접 조정할 수 있는 위치: 사장=전체, 출고 직원=창고, 매장·창고 직원=담당 위치
    private void checkLocation(Long locationId) {
        Admin admin = currentAdmin().orElseThrow(() -> new BusinessException(FORBIDDEN_LOCATION));
        Location location = locationRepo.getOrThrow(locationId);
        boolean allowed = switch (admin.getRole()) {
            case OWNER -> true;
            case FULFILLMENT -> location.getLocationType() == LocationType.WAREHOUSE;
            case STORE, WAREHOUSE -> admin.getLocation() != null
                    && admin.getLocation().getLocationId().equals(locationId);
        };
        if (!allowed) throw new BusinessException(FORBIDDEN_LOCATION);
    }

    private Optional<Admin> currentAdmin() {
        String loginId = AuthUtils.loginId();
        return loginId == null ? Optional.empty() : adminRepo.findByLoginId(loginId);
    }
}
