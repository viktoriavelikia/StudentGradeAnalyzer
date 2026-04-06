package com.gradeanalyzer.model;

/**
 * Представляет студента с именем и оценкой.
 * Допустимые значения оценок: 5 (отлично), 4 (хорошо), 3 (удовлетворительно), 0 (не допущен).
 */
public class Student {

    private final String fullName;   // Полное имя студента (ФИО)
    private final int grade;         // Оценка студента (0, 3, 4, 5)

    /**
     * Конструктор студента.
     *
     * @param fullName полное имя студента (ФИО)
     * @param grade    оценка студента (0, 3, 4, 5)
     * @throws IllegalArgumentException если имя пустое или оценка недопустимая
     */
    public Student(String fullName, int grade) {
        // Проверка, что имя не пустое и не состоит только из пробелов
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        // Проверка, что оценка соответствует допустимым значениям
        if (grade != 0 && grade != 3 && grade != 4 && grade != 5) {
            throw new IllegalArgumentException("Оценка должна быть 0, 3, 4 или 5. Получено: " + grade);
        }
        this.fullName = fullName;
        this.grade = grade;
    }

    /**
     * Возвращает полное имя студента.
     *
     * @return строка с ФИО
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Возвращает оценку студента.
     *
     * @return оценка (0, 3, 4, 5)
     */
    public int getGrade() {
        return grade;
    }

    /**
     * Проверяет, допущен ли студент к экзамену.
     *
     * @return true если студент допущен (оценка не равна 0), false если не допущен
     */
    public boolean isAdmitted() {
        return grade != 0;
    }

    /**
     * Строковое представление студента для отладки.
     *
     * @return строка вида "Student{fullName='...', grade=...}"
     */
    @Override
    public String toString() {
        return "Student{fullName='" + fullName + "', grade=" + grade + "}";
    }
}