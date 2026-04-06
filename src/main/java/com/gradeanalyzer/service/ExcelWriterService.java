package com.gradeanalyzer.service;

import com.gradeanalyzer.model.GroupStatistics;
import com.gradeanalyzer.model.Student;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Сервис для записи статистики группы в Excel файл.
 * Создает форматированный отчет с таблицей и столбчатой диаграммой.
 * Использует Apache POI 5.x XDDF API для создания графиков.
 */
public class ExcelWriterService {

    private static final Logger logger = LogManager.getLogger(ExcelWriterService.class);

    /**
     * Записывает результаты статистики в Excel файл по указанному пути.
     *
     * @param stats      вычисленная статистика группы
     * @param outputPath путь для сохранения файла .xlsx
     * @throws IOException если файл не удается записать
     */
    public void writeResults(GroupStatistics stats, String outputPath) throws IOException {
        logger.info("Запись результатов в файл: {}", outputPath);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Создаем два листа: для таблицы и для графика
            XSSFSheet summarySheet = workbook.createSheet("Результаты");
            XSSFSheet chartSheet = workbook.createSheet("График");

            writeSummarySheet(workbook, summarySheet, stats);
            writeChartSheet(workbook, chartSheet, stats);

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }

        logger.info("Результаты успешно сохранены в: {}", outputPath);
    }

    /**
     * Записывает сводную таблицу статистики и списки студентов.
     */
    private void writeSummarySheet(XSSFWorkbook workbook, XSSFSheet sheet, GroupStatistics stats) {
        CellStyle headerStyle = createHeaderStyle(workbook);   // Стиль для заголовков
        CellStyle titleStyle = createTitleStyle(workbook);     // Стиль для основного заголовка
        CellStyle dataStyle = createDataStyle(workbook);       // Стиль для данных

        // Основной заголовок таблицы
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Анализ успеваемости группы");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3)); // Объединяем 4 ячейки

        // Заголовки столбцов
        Row hRow = sheet.createRow(2);
        createStyledCell(hRow, 0, "Категория", headerStyle);
        createStyledCell(hRow, 1, "Количество", headerStyle);
        createStyledCell(hRow, 2, "Значение", headerStyle);

        // Заполняем данные
        int row = 3;
        writeDataRow(sheet, row++, "Отличники (5)", stats.countExcellent(), "", dataStyle);
        writeDataRow(sheet, row++, "Хорошисты (4)", stats.countGood(), "", dataStyle);
        writeDataRow(sheet, row++, "Троечники (3)", stats.countSatisfactory(), "", dataStyle);
        writeDataRow(sheet, row++, "Не допущены (0)", stats.countNotAdmitted(), "", dataStyle);
        writeDataRow(sheet, row++, "Средний балл", -1,
                String.format("%.2f", stats.getAverageGrade()), dataStyle);
        writeDataRow(sheet, row, "Максимальная оценка", stats.getMaxGrade(), "", dataStyle);

        // Устанавливаем ширину столбцов
        sheet.setColumnWidth(0, 7000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);

        // Записываем списки студентов по категориям
        int listStart = 11;
        listStart = writeNamedList(sheet, listStart, "Отличники (оценка 5)",
                stats.getExcellentStudents(), workbook);
        listStart = writeNamedList(sheet, listStart, "Хорошисты (оценка 4)",
                stats.getGoodStudents(), workbook);
        listStart = writeNamedList(sheet, listStart, "Троечники (оценка 3)",
                stats.getSatisfactoryStudents(), workbook);
        writeNamedList(sheet, listStart, "Не допущены к экзамену",
                stats.getNotAdmittedStudents(), workbook);
    }

    /**
     * Записывает список студентов с заголовком.
     *
     * @param sheet    лист для записи
     * @param startRow начальный номер строки
     * @param label    заголовок секции
     * @param students список студентов
     * @param workbook рабочая книга для создания стилей
     * @return следующий доступный номер строки
     */
    private int writeNamedList(XSSFSheet sheet, int startRow, String label,
                               List<Student> students, XSSFWorkbook workbook) {
        CellStyle labelStyle = createHeaderStyle(workbook);
        Row labelRow = sheet.createRow(startRow++);
        Cell labelCell = labelRow.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        if (students.isEmpty()) {
            // Если список пуст, выводим прочерк
            sheet.createRow(startRow++).createCell(0).setCellValue("—");
        } else {
            // Выводим ФИО и оценку каждого студента
            for (Student s : students) {
                Row r = sheet.createRow(startRow++);
                r.createCell(0).setCellValue(s.getFullName());
                r.createCell(1).setCellValue(s.getGrade());
            }
        }
        return startRow + 1; // Возвращаем следующую свободную строку
    }

    /**
     * Записывает данные для графика и создает столбчатую диаграмму.
     *
     * @param workbook рабочая книга
     * @param sheet    лист для графика
     * @param stats    статистика группы
     */
    private void writeChartSheet(XSSFWorkbook workbook, XSSFSheet sheet, GroupStatistics stats) {
        logger.debug("Создание листа с графиком");

        CellStyle headerStyle = createHeaderStyle(workbook);
        Row header = sheet.createRow(0);
        createStyledCell(header, 0, "Категория", headerStyle);
        createStyledCell(header, 1, "Количество", headerStyle);

        // Данные для графика
        String[] categories = {"Отличники (5)", "Хорошисты (4)", "Троечники (3)", "Не допущены"};
        int[] counts = {
                stats.countExcellent(), stats.countGood(),
                stats.countSatisfactory(), stats.countNotAdmitted()
        };

        // Заполняем данные
        for (int i = 0; i < categories.length; i++) {
            Row r = sheet.createRow(i + 1);
            r.createCell(0).setCellValue(categories[i]);
            r.createCell(1).setCellValue(counts[i]);
        }

        // Устанавливаем ширину столбцов
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);

        // Создаем столбчатую диаграмму
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, 1, 14, 20);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Распределение успеваемости группы");
        chart.setTitleOverlay(false);

        // Добавляем легенду внизу
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        // Создаем оси
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        // Источники данных для диаграммы
        XDDFDataSource<String> catData = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(1, 4, 0, 0));
        XDDFNumericalDataSource<Double> valData = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(1, 4, 1, 1));

        // Создаем столбчатую диаграмму (вертикальные столбцы)
        XDDFBarChartData barChart = (XDDFBarChartData)
                chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        barChart.setBarDirection(BarDirection.COL);

        // Добавляем серию данных
        XDDFBarChartData.Series series = (XDDFBarChartData.Series) barChart.addSeries(catData, valData);
        series.setTitle("Студентов", null);

        chart.plot(barChart);
        logger.info("Столбчатая диаграмма успешно создана");
    }

    /**
     * Записывает строку данных в таблицу.
     */
    private void writeDataRow(XSSFSheet sheet, int rowNum, String label, int count,
                              String extra, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        createStyledCell(row, 0, label, style);
        createStyledCell(row, 1, count >= 0 ? String.valueOf(count) : "", style);
        createStyledCell(row, 2, extra, style);
    }

    /**
     * Создает ячейку с заданным значением и стилем.
     */
    private void createStyledCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Создает стиль для заголовков таблицы.
     */
    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex()); // Голубой фон
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Создает стиль для основного заголовка.
     */
    private CellStyle createTitleStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Создает стиль для данных (с границами).
     */
    private CellStyle createDataStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}