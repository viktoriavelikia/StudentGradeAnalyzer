package com.gradeanalyzer.service;

import com.gradeanalyzer.model.GroupStatistics;
import com.gradeanalyzer.model.Student;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для анализа статистики успеваемости группы.
 * Использует Java Stream API для фильтрации, преобразования и агрегации данных.
 */
public class GradeAnalysisService {

    private static final Logger logger = LogManager.getLogger(GradeAnalysisService.class);

    // Константы для допустимых оценок
    private static final int EXCELLENT = 5;      // Отлично
    private static final int GOOD = 4;           // Хорошо
    private static final int SATISFACTORY = 3;   // Удовлетворительно
    private static final int NOT_ADMITTED = 0;   // Не допущен

    /**
     * Анализирует список студентов и возвращает статистику группы.
     *
     * @param students список студентов для анализа
     * @return объект {@link GroupStatistics} с сгруппированными и агрегированными данными
     * @throws IllegalArgumentException если список студентов пуст или равен null
     */
    public GroupStatistics analyze(List<Student> students) {
        // Проверка входных данных
        if (students == null || students.isEmpty()) {
            logger.error("Список студентов пуст или не загружен");
            throw new IllegalArgumentException("Список студентов пуст или не загружен");
        }

        logger.info("Анализ {} студентов", students.size());

        // Разделяем студентов по категориям оценок
        List<Student> excellent = filterByGrade(students, EXCELLENT);     // Отличники (5)
        List<Student> good = filterByGrade(students, GOOD);               // Хорошисты (4)
        List<Student> satisfactory = filterByGrade(students, SATISFACTORY); // Троечники (3)
        List<Student> notAdmitted = filterByGrade(students, NOT_ADMITTED);  // Не допущены (0)

        // Вычисляем средний балл ТОЛЬКО среди допущенных студентов (оценки 3,4,5)
        double average = students.stream()
                .filter(Student::isAdmitted)           // Оставляем только допущенных
                .mapToInt(Student::getGrade)           // Преобразуем в числа
                .average()                             // Вычисляем среднее
                .orElse(0.0);                          // Если нет допущенных, возвращаем 0

        // Находим максимальную оценку среди ВСЕХ студентов (включая недопущенных)
        int maxGrade = students.stream()
                .mapToInt(Student::getGrade)           // Преобразуем в числа
                .max()                                 // Находим максимум
                .orElse(0);                            // Если список пуст, возвращаем 0

        // Логируем результаты анализа
        logger.info("Анализ завершен: отличников={}, хорошистов={}, троечников={}, не допущены={}",
                excellent.size(), good.size(), satisfactory.size(), notAdmitted.size());
        logger.info("Средний балл: {}, Максимальная оценка: {}",
                String.format("%.2f", average), maxGrade);

        // Возвращаем объект со статистикой
        return new GroupStatistics(excellent, good, satisfactory, notAdmitted, average, maxGrade);
    }

    /**
     * Фильтрует студентов по заданной оценке с использованием Stream API.
     * Результат сортируется по ФИО в алфавитном порядке.
     *
     * @param students список всех студентов
     * @param grade    оценка для фильтрации (0, 3, 4, 5)
     * @return список студентов с указанной оценкой, отсортированный по имени
     */
    public List<Student> filterByGrade(List<Student> students, int grade) {
        return students.stream()
                .filter(s -> s.getGrade() == grade)                    // Фильтруем по оценке
                .sorted(Comparator.comparing(Student::getFullName))    // Сортируем по ФИО
                .collect(Collectors.toList());                         // Собираем в список
    }

    /**
     * Сортирует студентов по убыванию оценки, а при одинаковых оценках — по возрастанию ФИО.
     * Использует Stream API.
     *
     * @param students список студентов
     * @return отсортированный список
     */
    public List<Student> sortByGradeDesc(List<Student> students) {
        return students.stream()
                .sorted(Comparator.comparingInt(Student::getGrade).reversed()  // Сначала по убыванию оценки
                        .thenComparing(Student::getFullName))                  // Затем по возрастанию ФИО
                .collect(Collectors.toList());
    }
}