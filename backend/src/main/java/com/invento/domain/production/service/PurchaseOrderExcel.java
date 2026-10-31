package com.invento.domain.production.service;

import com.invento.domain.production.entity.ProductionOrder;
import com.invento.domain.production.entity.ProductionOrderItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

// 공장에 보내는 발주서 엑셀 (Apache POI). 단가·금액은 사장일 때만, 확정 납기일은 공장이 채워서 회신하는 빈칸
final class PurchaseOrderExcel {

    private PurchaseOrderExcel() {}

    static byte[] write(ProductionOrder o, boolean showCost) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("발주서");
            CellStyle title = style(wb, true, 18, false), bold = style(wb, true, 11, true), cell = style(wb, false, 11, true);
            CellStyle money = style(wb, false, 11, true);
            money.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
            List<String> headers = showCost ? List.of("No", "상품명", "상품번호", "수량", "단가", "금액") : List.of("No", "상품명", "상품번호", "수량");

            set(sheet.createRow(0), 0, "발 주 서", title);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));

            // 머리: 항목명 | 값 | 항목명 | 값
            String[][] info = {
                    {"발주번호", String.format("%03d", o.getProductionOrderId()), "발주일", String.valueOf(o.getOrderDate())},
                    {"공급처", o.getSupplier().getSupplierName(), "연락처", nvl(o.getSupplier().getContact())},
                    {"납기 희망일", nvl(o.getExpectedDate()), "확정 납기일", ""},
                    {"메모", nvl(o.getMemo()), "", ""}};
            for (int r = 0; r < info.length; r++) {
                Row row = sheet.createRow(2 + r);
                for (int c = 0; c < 4; c++) set(row, c, info[r][c], c % 2 == 0 ? bold : cell);
            }

            // 상품 표
            int start = 7;
            Row head = sheet.createRow(start);
            for (int c = 0; c < headers.size(); c++) set(head, c, headers.get(c), bold);
            int totalQty = 0;
            long totalAmount = 0;
            for (int i = 0; i < o.getItems().size(); i++) {
                ProductionOrderItem item = o.getItems().get(i);
                Row row = sheet.createRow(start + 1 + i);
                set(row, 0, i + 1, cell);
                set(row, 1, item.getProduct().getProductName(), cell);
                set(row, 2, item.getProduct().getProductCode(), cell);
                set(row, 3, item.getOrderedQty(), cell);
                if (showCost) {
                    long cost = item.getProduct().getCostPrice();
                    set(row, 4, cost, money);
                    set(row, 5, cost * item.getOrderedQty(), money);
                    totalAmount += cost * item.getOrderedQty();
                }
                totalQty += item.getOrderedQty();
            }
            Row total = sheet.createRow(start + 1 + o.getItems().size());
            set(total, 0, "합계", bold);
            set(total, 3, totalQty, bold);
            if (showCost) set(total, 5, totalAmount, money);

            sheet.createRow(total.getRowNum() + 2).createCell(0).setCellValue("확인 후 확정 납기일을 적어 회신 부탁드립니다.");
            int[] widths = {6, 30, 16, 10, 12, 14};
            for (int c = 0; c < headers.size(); c++) sheet.setColumnWidth(c, widths[c] * 256);

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static CellStyle style(Workbook wb, boolean bold, int size, boolean border) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) size);
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        if (border) {
            s.setBorderTop(BorderStyle.THIN);
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
        } else {
            s.setAlignment(HorizontalAlignment.CENTER);
        }
        return s;
    }

    private static void set(Row row, int col, Object value, CellStyle style) {
        Cell c = row.createCell(col);
        if (value instanceof Number n) c.setCellValue(n.doubleValue());
        else c.setCellValue(String.valueOf(value));
        c.setCellStyle(style);
    }

    private static String nvl(Object v) { return v == null ? "" : String.valueOf(v); }
}
