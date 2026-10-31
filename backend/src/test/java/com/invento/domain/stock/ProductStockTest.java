package com.invento.domain.stock;

import com.invento.domain.stock.entity.ProductStock;
import com.invento.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// DB 없이 도는 재고 수량 규칙 테스트
class ProductStockTest {

    @Test
    void 입고하면_보유수량이_늘어난다() {
        ProductStock stock = ProductStock.builder().build();
        stock.changeOnHand(10);
        assertThat(stock.getOnHandQty()).isEqualTo(10);
    }

    @Test
    void 보유수량보다_많이_빼면_예외() {
        ProductStock stock = ProductStock.builder().build();
        stock.changeOnHand(3);
        assertThatThrownBy(() -> stock.changeOnHand(-4)).isInstanceOf(BusinessException.class);
        assertThat(stock.getOnHandQty()).isEqualTo(3);
    }

    @Test
    void 판매가능수량은_보유에서_예약을_뺀_값() {
        ProductStock stock = ProductStock.builder().onHandQty(10).reservedQty(4).build();
        assertThat(stock.getAvailableQty()).isEqualTo(6);
    }
}
