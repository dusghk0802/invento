package com.invento.domain.order;

import com.invento.domain.order.entity.OrderStatus;
import com.invento.domain.order.entity.SalesOrder;
import com.invento.domain.product.entity.Product;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// DB 없이 도는 매장 영수증 금액·이익 계산 테스트
class SalesOrderTest {

    private SalesOrder receipt() {
        SalesOrder o = SalesOrder.builder().build();
        o.addItem(Product.builder().costPrice(1000).sellingPrice(3000).build(), 2); // 6000원, 이익 4000
        o.addItem(Product.builder().costPrice(500).sellingPrice(1500).build(), 1);  // 1500원, 이익 1000
        return o;
    }

    @Test
    void 받은_금액과_이익을_계산한다() {
        SalesOrder o = receipt();
        o.complete();
        assertThat(o.getReceivedAmount()).isEqualTo(7500);
        assertThat(o.getProductProfit()).isEqualTo(5000);
        assertThat(o.getFinalProfit()).isEqualTo(5000);
        assertThat(o.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void 판매_시점_가격으로_고정된다() {
        Product p = Product.builder().costPrice(1000).sellingPrice(3000).build();
        SalesOrder o = SalesOrder.builder().build();
        o.addItem(p, 1);
        p.update(null, null, null, null, null, null, null, null, false, 2000, 5000, null, null); // 나중에 가격 변경
        assertThat(o.getItems().get(0).getUnitPrice()).isEqualTo(3000);
    }
}
