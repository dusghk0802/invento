package com.invento.domain.order.service;

import com.invento.domain.admin.entity.Admin;
import com.invento.domain.admin.entity.AdminRole;
import com.invento.domain.admin.repository.AdminRepository;
import com.invento.domain.location.entity.Location;
import com.invento.domain.location.entity.LocationType;
import com.invento.domain.location.repository.LocationRepository;
import com.invento.domain.order.dto.StoreSaleDto.*;
import com.invento.domain.order.entity.OrderStatus;
import com.invento.domain.order.entity.SalesOrder;
import com.invento.domain.order.repository.SalesOrderRepository;
import com.invento.domain.product.repository.ProductRepository;
import com.invento.domain.stock.service.StockService;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.invento.domain.order.entity.SalesChannel.OFFLINE;
import static com.invento.domain.stock.entity.StockMovementType.SALE_OUT;
import static com.invento.global.exception.ErrorCode.FORBIDDEN_LOCATION;
import static com.invento.global.exception.ErrorCode.STORE_ONLY;

// 매장 판매: 영수증 저장 즉시 매장 재고 차감 (SALE_OUT). 사장 + 매장 직원(담당 매장만)
@Service
@RequiredArgsConstructor
@Transactional
public class StoreSaleService {

    private final SalesOrderRepository repo;
    private final ProductRepository productRepo;
    private final LocationRepository locationRepo;
    private final AdminRepository adminRepo;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<Summary> findAll(Search s) {
        Admin me = adminRepo.current();
        boolean owner = me.getRole() == AdminRole.OWNER;
        Long locationId = owner ? s.locationId() : myLocationId(me);
        LocalDate from = s.from() == null ? LocalDate.now() : s.from();
        LocalDate to = s.to() == null ? from : s.to();
        return repo.findByChannelAndOrderDateBetweenOrderByOrderIdDesc(OFFLINE, from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream().filter(o -> locationId == null || o.getLocation().getLocationId().equals(locationId))
                .map(o -> Summary.of(o, owner)).toList();
    }

    @Transactional(readOnly = true)
    public Detail findOne(Long id) {
        Admin me = adminRepo.current();
        SalesOrder o = repo.getOrThrow(id);
        boolean owner = me.getRole() == AdminRole.OWNER;
        if (!owner && !o.getLocation().getLocationId().equals(myLocationId(me))) throw new BusinessException(FORBIDDEN_LOCATION);
        return Detail.of(o, owner);
    }

    public Detail create(Request r) {
        stockService.checkLocation(r.locationId()); // 매장 직원은 담당 매장만
        Location location = locationRepo.getOrThrow(r.locationId());
        if (location.getLocationType() != LocationType.STORE) throw new BusinessException(STORE_ONLY);
        Admin me = adminRepo.current();
        LocalDateTime now = LocalDateTime.now();
        SalesOrder o = SalesOrder.builder().channel(OFFLINE).orderNo(nextReceiptNo(now)).location(location)
                .orderDate(now).status(OrderStatus.COMPLETED).paymentMethod(r.paymentMethod()).createdBy(me.getAdminId()).build();
        r.items().forEach(i -> o.addItem(productRepo.getOrThrow(i.productId()), i.quantity()));
        o.complete();
        repo.save(o);
        o.getItems().forEach(i -> stockService.change(i.getProduct().getProductId(), location.getLocationId(), SALE_OUT,
                -i.getQuantity(), null, "SALES_ORDER", o.getOrderId(), null));
        return Detail.of(o, me.getRole() == AdminRole.OWNER);
    }

    // 영수증 번호: R + 날짜 + 그날 매장 영수증 순번 (예: R261031-0007)
    private String nextReceiptNo(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        long seq = repo.countByChannelAndOrderDateBetween(OFFLINE, today.atStartOfDay(), today.plusDays(1).atStartOfDay()) + 1;
        return "R" + today.format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + String.format("%04d", seq);
    }

    private Long myLocationId(Admin me) { return me.getLocation() == null ? -1L : me.getLocation().getLocationId(); }
}
