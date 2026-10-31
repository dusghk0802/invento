package com.invento.domain.production;

import com.invento.domain.production.entity.ProductionOrder;
import com.invento.domain.production.entity.ProductionStatus;
import com.invento.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// DB 없이 도는 발주 상태 자동 변경 규칙 테스트
class ProductionOrderTest {

    private ProductionOrder orderOf(int... qtys) {
        ProductionOrder o = ProductionOrder.builder().orderDate(LocalDate.now()).build();
        for (int q : qtys) o.addItem(null, q);
        return o;
    }

    @Test
    void 일부만_입고하면_일부_입고() {
        ProductionOrder o = orderOf(10, 5);
        o.getItems().get(0).receive(10, 0);
        o.refreshStatus();
        assertThat(o.getStatus()).isEqualTo(ProductionStatus.PARTIAL);
    }

    @Test
    void 정상과_불량을_합쳐_전부_처리되면_입고_완료() {
        ProductionOrder o = orderOf(10);
        o.getItems().get(0).receive(8, 2);
        o.refreshStatus();
        assertThat(o.getStatus()).isEqualTo(ProductionStatus.RECEIVED);
    }

    @Test
    void 남은_수량보다_많이_입고하면_예외() {
        ProductionOrder o = orderOf(10);
        o.getItems().get(0).receive(7, 0);
        assertThatThrownBy(() -> o.getItems().get(0).receive(3, 1)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 입고가_시작된_발주는_수정할_수_없다() {
        ProductionOrder o = orderOf(10);
        o.getItems().get(0).receive(1, 0);
        o.refreshStatus();
        assertThatThrownBy(() -> o.update(null, LocalDate.now(), null, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 입고_수량을_줄이면_다시_일부_입고로_돌아간다() {
        ProductionOrder o = orderOf(10);
        o.getItems().get(0).receive(10, 0);
        o.refreshStatus();
        o.getItems().get(0).correct(-3, 0); // 10개로 잘못 입력 → 7개로 수정
        o.refreshStatus();
        assertThat(o.getItems().get(0).getReceivedQty()).isEqualTo(7);
        assertThat(o.getStatus()).isEqualTo(ProductionStatus.PARTIAL);
    }

    @Test
    void 입고_수량을_발주보다_많게_수정하면_예외() {
        ProductionOrder o = orderOf(10);
        o.getItems().get(0).receive(9, 0);
        assertThatThrownBy(() -> o.getItems().get(0).correct(2, 0)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 예정일이_지났는데_남은_수량이_있으면_지연() {
        ProductionOrder o = ProductionOrder.builder().orderDate(LocalDate.now().minusDays(10))
                .expectedDate(LocalDate.now().minusDays(1)).build();
        o.addItem(null, 10);
        assertThat(o.isDelayed()).isTrue();
    }

    @Test
    void 공장_확인하면_확정_납기일로_지연을_판단한다() {
        ProductionOrder o = ProductionOrder.builder().orderDate(LocalDate.now().minusDays(10))
                .expectedDate(LocalDate.now().minusDays(1)).build(); // 희망일은 지났지만
        o.addItem(null, 10);
        o.confirm(LocalDate.now().plusDays(3)); // 공장 회신 납기는 3일 뒤
        assertThat(o.getStatus()).isEqualTo(ProductionStatus.CONFIRMED);
        assertThat(o.isDelayed()).isFalse();
    }
}
