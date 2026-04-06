package com.gradeanalyzer.model;

import java.util.List;
import java.util.Objects;

/**
 * Хранит результаты статистического анализа успеваемости группы.
 */
public class GroupStatistics {

    private final List<Student> excellentStudents;      // Студенты с оценкой 5 (отличники)
    private final List<Student> goodStudents;           // Студенты с оценкой 4 (хорошисты)
    private final List<Student> satisfactoryStudents;   // Студенты с оценкой 3 (троечники)
    private final List<Student> notAdmittedStudents;    // Студенты, не допущенные к экзамену (оценка 0)
    private final double averageGrade;                  // Средний балл по группе (только допущенные)
    private final int maxGrade;                         // Максимальная оценка в группе

    /**
     * Конструктор объекта статистики группы.
     *
     * @param excellentStudents     список студентов с оценкой 5
     * @param goodStudents          список студентов с оценкой 4
     * @param satisfactoryStudents  список студентов с оценкой 3
     * @param notAdmittedStudents   список студентов, не допущенных к экзамену
     * @param averageGrade          средний балл по допущенным студентам
     * @param maxGrade              максимальная оценка в группе
     */
    public GroupStatistics(List<Student> excellentStudents,
                           List<Student> goodStudents,
                           List<Student> satisfactoryStudents,
                           List<Student> notAdmittedStudents,
                           double averageGrade,
                           int maxGrade) {

        // Проверка, что списки не null (защита от ошибок)
        this.excellentStudents = Objects.requireNonNull(excellentStudents, "excellentStudents cannot be null");
        this.goodStudents = Objects.requireNonNull(goodStudents, "goodStudents cannot be null");
        this.satisfactoryStudents = Objects.requireNonNull(satisfactoryStudents, "satisfactoryStudents cannot be null");
        this.notAdmittedStudents = Objects.requireNonNull(notAdmittedStudents, "notAdmittedStudents cannot be null");

        // Проверка, что максимальная оценка имеет допустимое значение (0, 3, 4, 5)
        if (maxGrade != 0 && maxGrade != 3 && maxGrade != 4 && maxGrade != 5) {
            throw new IllegalArgumentException("maxGrade must be 0, 3, 4, or 5. Got: " + maxGrade);
        }

        this.averageGrade = averageGrade;
        this.maxGrade = maxGrade;
    }

    /** @return список студентов с оценкой 5 (отличники) */
    public List<Student> getExcellentStudents() { return excellentStudents; }

    /** @return список студентов с оценкой 4 (хорошисты) */
    public List<Student> getGoodStudents() { return goodStudents; }

    /** @return список студентов с оценкой 3 (троечники) */
    public List<Student> getSatisfactoryStudents() { return satisfactoryStudents; }

    /** @return список студентов, не допущенных к экзамену */
    public List<Student> getNotAdmittedStudents() { return notAdmittedStudents; }

    /** @return средний балл по допущенным студентам */
    public double getAverageGrade() { return averageGrade; }

    /** @return максимальная оценка в группе */
    public int getMaxGrade() { return maxGrade; }

    /** @return количество отличников (оценка 5) */
    public int countExcellent() { return excellentStudents.size(); }

    /** @return количество хорошистов (оценка 4) */
    public int countGood() { return goodStudents.size(); }

    /** @return количество троечников (оценка 3) */
    public int countSatisfactory() { return satisfactoryStudents.size(); }

    /** @return количество студентов, не допущенных к экзамену */
    public int countNotAdmitted() { return notAdmittedStudents.size(); }
}