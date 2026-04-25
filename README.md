# Анализ успеваемости студентов

## Описание
Приложение читает Excel-файл с оценками студентов, анализирует успеваемость группы
и сохраняет результаты в новый Excel-файл с таблицами и графиком.

## Структура проекта
```
StudentGradeAnalyzer/
├── build.gradle
├── settings.gradle
├── sample_input.xlsx          ← Пример входного файла
├── src/
│   ├── main/
│   │   ├── java/com/gradeanalyzer/
│   │   │   ├── model/
│   │   │   │   ├── Student.java
│   │   │   │   └── GroupStatistics.java
│   │   │   ├── service/
│   │   │   │   ├── ExcelReaderService.java
│   │   │   │   ├── GradeAnalysisService.java
│   │   │   │   └── ExcelWriterService.java
│   │   │   └── ui/
│   │   │       └── MainApp.java
│   │   └── resources/
│   │       └── log4j2.xml
│   └── test/
│       └── java/com/gradeanalyzer/
│           └── GradeAnalysisServiceTest.java
```

## Формат входного Excel файла
- Колонка A: ФИО студента
- Колонка B: Оценка (5, 4, 3, 0 — не допущен)
- Первая строка — заголовок (пропускается)

## Запуск в IntelliJ IDEA Community Edition 2022

### Требования
- Java 17 (Amazon Corretto 17 или Eclipse Temurin 17)
- IntelliJ IDEA Community Edition 2022
- Gradle (встроенный в IntelliJ)
