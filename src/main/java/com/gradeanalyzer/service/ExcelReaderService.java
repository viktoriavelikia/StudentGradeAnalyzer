package com.gradeanalyzer.service;

import com.gradeanalyzer.model.Student;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для чтения данных об успеваемости студентов из Excel файла (.xlsx).
 * <p>
 * Ожидаемый формат: столбец A = ФИО, столбец B = Оценка.
 * Первая строка считается заголовком и пропускается.
 * Оценка 0 означает, что студент не допущен к экзамену.
 * </p>
 */
public class ExcelReaderService {

    private static final Logger logger = LogManager.getLogger(ExcelReaderService.class);

    // Константы для индексов столбцов
    private static final int COLUMN_NAME = 0;      // Столбец с ФИО (столбец A)
    private static final int COLUMN_GRADE = 1;     // Столбец с оценкой (столбец B)
    private static final int HEADER_ROW_INDEX = 0; // Индекс строки заголовка (первая строка)

    /**
     * Читает студентов из указанного Excel файла.
     *
     * @param filePath путь к Excel файлу
     * @return список объектов {@link Student}, полученных из файла
     * @throws IOException              если файл не удается прочитать
     * @throws IllegalArgumentException если формат файла не поддерживается
     */
    public List<Student> readStudents(String filePath) throws IOException {
        logger.info("Чтение студентов из файла: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("Файл не найден: {}", filePath);
            throw new IOException("Файл не найден: " + filePath);
        }
        if (!filePath.endsWith(".xlsx")) {
            logger.error("Неподдерживаемый формат файла: {}", filePath);
            throw new IllegalArgumentException("Поддерживается только формат .xlsx");
        }

        List<Student> students = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel файл не содержит листов");
            }

            // Пропускаем строку заголовка (первую строку)
            for (int i = HEADER_ROW_INDEX + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue; // Пропускаем пустые строки

                // Получаем ФИО из столбца A и оценку из столбца B
                String name = getCellStringValue(row.getCell(COLUMN_NAME));
                int grade = getCellGradeValue(row.getCell(COLUMN_GRADE));

                if (name == null || name.isBlank()) {
                    logger.warn("Пропуск строки {} из-за пустого ФИО", i + 1);
                    continue;
                }

                students.add(new Student(name.trim(), grade));
                logger.debug("Студент добавлен: {} с оценкой {}", name, grade);
            }
        }

        logger.info("Успешно прочитано {} студентов из файла", students.size());
        return students;
    }

    /**
     * Извлекает текстовое значение из ячейки.
     *
     * @param cell ячейка Excel
     * @return строковое представление или null, если ячейка пустая
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> null;
        };
    }

    /**
     * Извлекает значение оценки из ячейки.
     * Возвращает 0, если ячейка содержит нечисловое значение (считается "не допущен").
     *
     * @param cell ячейка Excel
     * @return целочисленное значение оценки (0, 3, 4, 5)
     */
    private int getCellGradeValue(Cell cell) {
        if (cell == null) return 0;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> {
                    String val = cell.getStringCellValue().trim();
                    // Обработка текстовых обозначений для недопущенных студентов
                    if (val.equalsIgnoreCase("н/д") || val.equalsIgnoreCase("не допущен")
                            || val.isBlank()) {
                        yield 0;
                    }
                    yield Integer.parseInt(val);
                }
                default -> 0;
            };
        } catch (NumberFormatException e) {
            logger.warn("Не удалось распознать значение оценки, считаем как не допущен");
            return 0;
        }
    }
}