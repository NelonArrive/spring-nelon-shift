package nelon.arrive.nelonshift.services;

import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Shift;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProjectExcelService {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  public byte[] exportProjectToExcel(Project project) throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(project.getName());

    // Создаём стили
    CellStyle headerStyle = createHeaderStyle(workbook);
    CellStyle dateHeaderStyle = createDateHeaderStyle(workbook);
    CellStyle timeStyle = createTimeStyle(workbook);
    CellStyle dataStyle = createDataStyle(workbook);
    CellStyle currencyStyle = createCurrencyStyle(workbook);
    CellStyle totalStyle = createTotalStyle(workbook);
    CellStyle grandTotalStyle = createGrandTotalStyle(workbook);

    int rowNum = 0;

    // Заголовок проекта
    Row titleRow = sheet.createRow(rowNum++);
    Cell titleCell = titleRow.createCell(0);
    titleCell.setCellValue("Проект: " + project.getName());
    titleCell.setCellStyle(headerStyle);
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

    rowNum++; // Пустая строка

    // Заголовки колонок
    Row headerRow = sheet.createRow(rowNum++);
    String[] headers = {"Дата", "Время", "Смена (часы)", "Переработки (часы)",
        "Суточные", "Компенсации", "ИТОГО"};

    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    // Сортируем смены по дате
    List<Shift> shifts = project.getShifts().stream()
        .sorted((s1, s2) -> s1.getDate().compareTo(s2.getDate()))
        .toList();

    int dataStartRow = rowNum;

    // Данные по сменам
    for (Shift shift : shifts) {
      Row row = sheet.createRow(rowNum++);

      // Дата
      Cell dateCell = row.createCell(0);
      dateCell.setCellValue(shift.getDate().format(DATE_FORMATTER));
      dateCell.setCellStyle(dateHeaderStyle);

      // Время
      Cell timeCell = row.createCell(1);
      String timeRange = formatTimeRange(shift.getStartTime(), shift.getEndTime());
      timeCell.setCellValue(timeRange);
      timeCell.setCellStyle(timeStyle);

      // Часы смены
      Cell hoursCell = row.createCell(2);
      hoursCell.setCellValue(shift.getHours() != null ? shift.getHours() : 0);
      hoursCell.setCellStyle(dataStyle);

      // Переработки
      Cell overtimeCell = row.createCell(3);
      overtimeCell.setCellValue(shift.getOvertimeHours() != null ? shift.getOvertimeHours() : 0);
      overtimeCell.setCellStyle(dataStyle);

      // Суточные
      Cell perDiemCell = row.createCell(4);
      if (shift.getPerDiem() != null) {
        perDiemCell.setCellValue(shift.getPerDiem().doubleValue());
      } else {
        perDiemCell.setCellValue(0);
      }
      perDiemCell.setCellStyle(currencyStyle);

      // Компенсации (базовая оплата + оплата переработок)
      Cell compensationCell = row.createCell(5);
      BigDecimal compensation = BigDecimal.ZERO;
      if (shift.getBasePay() != null) {
        compensation = compensation.add(shift.getBasePay());
      }
      if (shift.getOvertimePay() != null) {
        compensation = compensation.add(shift.getOvertimePay());
      }
      compensationCell.setCellValue(compensation.doubleValue());
      compensationCell.setCellStyle(currencyStyle);

      // ИТОГО (формула: Суточные + Компенсации)
      Cell totalCell = row.createCell(6);
      int currentRow = row.getRowNum() + 1; // Excel rows start from 1
      totalCell.setCellFormula("E" + currentRow + "+F" + currentRow);
      totalCell.setCellStyle(currencyStyle);
    }

    int dataEndRow = rowNum - 1;

    // Пустая строка
    rowNum++;

    // Строка ИТОГО (сумма всех смен)
    Row subtotalRow = sheet.createRow(rowNum++);

    Cell subtotalLabelCell = subtotalRow.createCell(0);
    subtotalLabelCell.setCellValue("ИТОГО:");
    subtotalLabelCell.setCellStyle(totalStyle);

    // Сумма часов
    Cell totalHoursCell = subtotalRow.createCell(2);
    if (dataEndRow >= dataStartRow) {
      totalHoursCell.setCellFormula("SUM(C" + (dataStartRow + 1) + ":C" + (dataEndRow + 1) + ")");
    } else {
      totalHoursCell.setCellValue(0);
    }
    totalHoursCell.setCellStyle(totalStyle);

    // Сумма переработок
    Cell totalOvertimeCell = subtotalRow.createCell(3);
    if (dataEndRow >= dataStartRow) {
      totalOvertimeCell.setCellFormula("SUM(D" + (dataStartRow + 1) + ":D" + (dataEndRow + 1) + ")");
    } else {
      totalOvertimeCell.setCellValue(0);
    }
    totalOvertimeCell.setCellStyle(totalStyle);

    // Сумма суточных
    Cell totalPerDiemCell = subtotalRow.createCell(4);
    if (dataEndRow >= dataStartRow) {
      totalPerDiemCell.setCellFormula("SUM(E" + (dataStartRow + 1) + ":E" + (dataEndRow + 1) + ")");
    } else {
      totalPerDiemCell.setCellValue(0);
    }
    totalPerDiemCell.setCellStyle(totalStyle);

    // Сумма компенсаций
    Cell totalCompensationCell = subtotalRow.createCell(5);
    if (dataEndRow >= dataStartRow) {
      totalCompensationCell.setCellFormula("SUM(F" + (dataStartRow + 1) + ":F" + (dataEndRow + 1) + ")");
    } else {
      totalCompensationCell.setCellValue(0);
    }
    totalCompensationCell.setCellStyle(totalStyle);

    // Общая сумма
    Cell grandTotalCell = subtotalRow.createCell(6);
    if (dataEndRow >= dataStartRow) {
      grandTotalCell.setCellFormula("SUM(G" + (dataStartRow + 1) + ":G" + (dataEndRow + 1) + ")");
    } else {
      grandTotalCell.setCellValue(0);
    }
    grandTotalCell.setCellStyle(totalStyle);

    // Строка ИТОГО С НАЛОГОМ (умножаем на 100 и делим на 94)
    Row taxTotalRow = sheet.createRow(rowNum++);

    Cell taxLabelCell = taxTotalRow.createCell(0);
    taxLabelCell.setCellValue("ИТОГО С НАЛОГОМ:");
    taxLabelCell.setCellStyle(grandTotalStyle);
    sheet.addMergedRegion(new CellRangeAddress(taxTotalRow.getRowNum(),
        taxTotalRow.getRowNum(), 0, 5));

    Cell taxTotalCell = taxTotalRow.createCell(6);
    int subtotalRowNum = subtotalRow.getRowNum() + 1; // Excel row number
    // Формула: ИТОГО * 100 / 94
    taxTotalCell.setCellFormula("G" + subtotalRowNum + "*100/94");
    taxTotalCell.setCellStyle(grandTotalStyle);

    // Автоматическая ширина колонок
    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
      // Добавляем немного отступа
      int currentWidth = sheet.getColumnWidth(i);
      sheet.setColumnWidth(i, currentWidth + 1000);
    }

    // Принудительно пересчитываем формулы
    workbook.setForceFormulaRecalculation(true);

    // Конвертируем в byte array
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();

    return outputStream.toByteArray();
  }

  private String formatTimeRange(LocalTime startTime, LocalTime endTime) {
    if (startTime == null && endTime == null) {
      return "-";
    }
    if (startTime == null) {
      return "- до " + endTime.format(TIME_FORMATTER);
    }
    if (endTime == null) {
      return startTime.format(TIME_FORMATTER) + " - ";
    }
    return startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER);
  }

  // Стили

  private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 12);
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);

    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    setBorders(style);

    return style;
  }

  private CellStyle createDateHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 10);
    style.setFont(font);

    style.setAlignment(HorizontalAlignment.LEFT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    setBorders(style);

    return style;
  }

  private CellStyle createTimeStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();

    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    setBorders(style);

    return style;
  }

  private CellStyle createDataStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();

    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    setBorders(style);

    return style;
  }

  private CellStyle createCurrencyStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();

    // Формат валюты: #,##0.00 ₽
    DataFormat format = workbook.createDataFormat();
    style.setDataFormat(format.getFormat("#,##0.00 ₽"));

    style.setAlignment(HorizontalAlignment.RIGHT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    setBorders(style);

    return style;
  }

  private CellStyle createTotalStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 11);
    style.setFont(font);

    DataFormat format = workbook.createDataFormat();
    style.setDataFormat(format.getFormat("#,##0.00 ₽"));

    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    style.setAlignment(HorizontalAlignment.RIGHT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    setBorders(style);

    return style;
  }

  private CellStyle createGrandTotalStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 12);
    font.setColor(IndexedColors.DARK_RED.getIndex());
    style.setFont(font);

    DataFormat format = workbook.createDataFormat();
    style.setDataFormat(format.getFormat("#,##0.00 ₽"));

    style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    style.setAlignment(HorizontalAlignment.RIGHT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    setBorders(style);

    return style;
  }

  private void setBorders(CellStyle style) {
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);

    style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
    style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
    style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
    style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
  }
}