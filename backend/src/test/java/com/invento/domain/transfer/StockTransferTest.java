package com.invento.domain.transfer;

import com.invento.domain.product.entity.Product;
import com.invento.domain.transfer.entity.StockTransfer;
import com.invento.domain.transfer.entity.TransferStatus;
import com.invento.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// DB 없이 도는 재고 이동 규칙 테스트
class StockTransferTest {

    @Test
    void 박스_수와_낱개로_이동_수량을_계산한다() {
        StockTransfer t = StockTransfer.builder().build();
        t.addItem(Product.builder().boxQty(40).build(), 2, 5);
        assertThat(t.getItems().get(0).getQuantity()).isEqualTo(85);
    }

    @Test
    void 박스_입수량이_없는_상품은_박스로_입력할_수_없다() {
        StockTransfer t = StockTransfer.builder().build();
        assertThatThrownBy(() -> t.addItem(Product.builder().build(), 1, 0)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 요청_승인_출고_도착_순서로만_진행된다() {
        StockTransfer t = StockTransfer.builder().build();
        assertThatThrownBy(() -> t.ship(1L)).isInstanceOf(BusinessException.class); // 승인 전 출고 불가
        t.approve(1L);
        t.ship(1L);
        assertThatThrownBy(t::cancel).isInstanceOf(BusinessException.class); // 출고 후 취소 불가
        t.receive(2L);
        assertThat(t.getStatus()).isEqualTo(TransferStatus.RECEIVED);
    }

    @Test
    void 반려하면_사유가_남고_출고할_수_없다() {
        StockTransfer t = StockTransfer.builder().build();
        t.reject(1L, "창고 재고 없음");
        assertThat(t.getRejectReason()).isEqualTo("창고 재고 없음");
        assertThatThrownBy(() -> t.ship(1L)).isInstanceOf(BusinessException.class);
    }
}
