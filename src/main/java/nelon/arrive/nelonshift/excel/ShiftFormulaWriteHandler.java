package nelon.arrive.nelonshift.excel;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.*;

public class ShiftFormulaWriteHandler implements RowWriteHandler {

  private final int dataStartRow;
  private int lastDataRow = -1;

  public ShiftFormulaWriteHandler(int dataStartRow) {
    this.dataStartRow = dataStartRow;
  }

  @Override
  public void afterRowDispose(WriteSheetHolder writeSheetHolder,
                              WriteTableHolder writeTableHolder,
                              Row row,
                              Integer relativeRowIndex,
                              Boolean isHead) {

    if (isHead) {
      return;
    }

    int rowNum = row.getRowNum();
    lastDataRow = rowNum;

    Sheet sheet = writeSheetHolder.getSheet();

    // Колонка F (index 6) - Всего часов = C + D (Смена + Переработки)
    Cell totalHoursCell = row.createCell(6);
    totalHoursCell.setCellFormula("C" + (rowNum + 1) + "+D" + (rowNum + 1));

    // Колонка G (index 7) - Сумма = E + F (Суточные + Компенсации)
    Cell totalSumCell = row.createCell(7);
    totalSumCell.setCellFormula("E" + (rowNum + 1) + "+F" + (rowNum + 1));

    // Применяем стиль для формул
    CellStyle numberStyle = writeSheetHolder.getSheet().getWorkbook().createCellStyle();
    DataFormat format = writeSheetHolder.getSheet().getWorkbook().createDataFormat();
    numberStyle.setDataFormat(format.getFormat("#,##0.00"));
    numberStyle.setAlignment(HorizontalAlignment.RIGHT);
    totalSumCell.setCellStyle(numberStyle);

    CellStyle hoursStyle = writeSheetHolder.getSheet().getWorkbook().createCellStyle();
    hoursStyle.setAlignment(HorizontalAlignment.CENTER);
    totalHoursCell.setCellStyle(hoursStyle);
  }

  @Override
  public void afterRowCreate(
      WriteSheetHolder writeSheetHolder,
      WriteTableHolder writeTableHolder,
      Row row,
      Integer relativeRowIndex,
      Boolean isHead
  ) {
  }

  /**
   * Добавляет итоговые строки после всех данных
   */
  public void addTotalRows(Sheet sheet, int dataStartRow, int lastDataRow) {
    Workbook workbook = sheet.getWorkbook();

    // Пустая строка перед итогами
    int emptyRowNum = lastDataRow + 1;

    // Строка "ИТОГО:"
    int totalRowNum = lastDataRow + 2;
    Row totalRow = sheet.createRow(totalRowNum);

    // Ячейка с текстом "ИТОГО:"
    Cell totalLabelCell = totalRow.createCell(5);
    totalLabelCell.setCellValue("ИТОГО:");

    CellStyle boldStyle = workbook.createCellStyle();
    Font boldFont = workbook.createFont();
    boldFont.setBold(true);
    boldFont.setFontHeightInPoints((short) 11);
    boldStyle.setFont(boldFont);
    boldStyle.setAlignment(HorizontalAlignment.RIGHT);
    totalLabelCell.setCellStyle(boldStyle);

    // Сумма всех часов
    Cell totalHoursCell = totalRow.createCell(6);
    totalHoursCell.setCellFormula("SUM(G" + (dataStartRow + 1) + ":G" + (lastDataRow + 1) + ")");

    CellStyle hoursSumStyle = workbook.createCellStyle();
    hoursSumStyle.setFont(boldFont);
    hoursSumStyle.setAlignment(HorizontalAlignment.CENTER);
    totalHoursCell.setCellStyle(hoursSumStyle);

    // Общая сумма
    Cell totalSumCell = totalRow.createCell(7);
    totalSumCell.setCellFormula("SUM(H" + (dataStartRow + 1) + ":H" + (lastDataRow + 1) + ")");

    CellStyle sumStyle = workbook.createCellStyle();
    sumStyle.setFont(boldFont);
    DataFormat format = workbook.createDataFormat();
    sumStyle.setDataFormat(format.getFormat("#,##0.00"));
    sumStyle.setAlignment(HorizontalAlignment.RIGHT);
    totalSumCell.setCellStyle(sumStyle);

    // Строка "ИТОГО с налогом (6%):"
    int taxRowNum = totalRowNum + 1;
    Row taxRow = sheet.createRow(taxRowNum);

    Cell taxLabelCell = taxRow.createCell(5);
    taxLabelCell.setCellValue("ИТОГО с налогом (6%):");
    taxLabelCell.setCellStyle(boldStyle);

    // Формула: (СУММА * 100) / 94
    Cell taxSumCell = taxRow.createCell(7);
    taxSumCell.setCellFormula("(H" + (totalRowNum + 1) + "*100)/94");

    CellStyle taxStyle = workbook.createCellStyle();
    taxStyle.setFont(boldFont);
    taxStyle.setDataFormat(format.getFormat("#,##0.00"));
    taxStyle.setAlignment(HorizontalAlignment.RIGHT);

    // Заливка для итоговой строки с налогом
    taxStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
    taxStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    taxSumCell.setCellStyle(taxStyle);
  }
}