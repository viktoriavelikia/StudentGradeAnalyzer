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

### Шаги
1. Откройте папку `StudentGradeAnalyzer` через **File → Open**
2. IntelliJ автоматически импортирует Gradle проект
3. Подождите пока скачаются зависимости
4. Для запуска: нажмите ▶ рядом с методом `main` в `MainApp.java`
   (или Tasks → application → run в Gradle панели)
5. Для сборки JAR: Tasks → build → jar

### Возможные проблемы
- Если JavaFX не найден: убедитесь что JDK 17+ выбран в **File → Project Structure → SDK**
- Если ошибка Gradle: **File → Invalidate Caches → Invalidate and Restart**

## Критерии оценки — соответствие

| Критерий | Реализация |
|---|---|
| 1.1 Код работает по заданию | ✅ Полное соответствие ТЗ |
| 1.2 JAR файл | ✅ `gradle jar` → fat JAR |
| 2.3 Нет аварийных завершений | ✅ try/catch везде |
| 2.4 Обработка исключений | ✅ IOException, IllegalArgumentException |
| 4.1 Log4j логирование | ✅ log4j2.xml + консоль + файл |
| 4.2 Gradle | ✅ build.gradle |
| 4.3 JUnit тесты | ✅ 8 тестов |
| 4.4 JavaFX UI | ✅ MainApp.java |
| 4.5 JavaDoc | ✅ На всех публичных методах |
| 4.6 Stream API | ✅ В GradeAnalysisService |

## Используемые библиотеки
- **Apache POI 5.2.3** — чтение и запись Excel файлов, создание графиков
- **Log4j2 2.20.0** — логирование в консоль и файл
- **JUnit 5.9.3** — модульное тестирование
- **JavaFX 17** — графический интерфейс
