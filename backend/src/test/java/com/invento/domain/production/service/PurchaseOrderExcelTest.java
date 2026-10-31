package com.invento.domain.production.service;

import com.invento.domain.product.entity.Product;
import com.invento.domain.production.entity.ProductionOrder;
import com.invento.domain.supplier.entity.Supplier;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// DB 없이 도는 발주서 엑셀 테스트
class PurchaseOrderExcelTest {

    private ProductionOrder order() {
        ProductionOrder o = ProductionOrder.builder().supplier(Supplier.builder().supplierName("한빛인쇄").build())
                .orderDate(LocalDate.of(2026, 10, 31)).build();
        o.addItem(Product.builder().productName("스티커").productCode("ST-01").costPrice(300).build(), 100);
        return o;
    }

    private Sheet read(byte[] bytes) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes)).getSheetAt(0);
    }

    @Test
    void 사장이면_단가와_금액이_들어간다() throws IOException {
        Sheet sheet = read(PurchaseOrderExcel.write(order(), true));
        assertThat(sheet.getRow(3).getCell(1).getStringCellValue()).isEqualTo("한빛인쇄");
        assertThat(sheet.getRow(7).getCell(5).getStringCellValue()).isEqualTo("금액");
        assertThat(sheet.getRow(8).getCell(5).getNumericCellValue()).isEqualTo(30000);
    }

    @Test
    void 직원이면_단가_컬럼이_없다() throws IOException {
        Sheet sheet = read(PurchaseOrderExcel.write(order(), false));
        assertThat(sheet.getRow(7).getLastCellNum()).isEqualTo((short) 4);
        assertThat(sheet.getRow(8).getCell(3).getNumericCellValue()).isEqualTo(100);
    }
}
