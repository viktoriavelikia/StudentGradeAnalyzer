package com.gradeanalyzer;

import com.gradeanalyzer.model.GroupStatistics;
import com.gradeanalyzer.model.Student;
import com.gradeanalyzer.service.GradeAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GradeAnalysisService}.
 * Verifies correct filtering, aggregation, and sorting behavior.
 */
public class GradeAnalysisServiceTest {

    private GradeAnalysisService service;
    private List<Student> testStudents;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    public void setUp() {
        service = new GradeAnalysisService();
        testStudents = Arrays.asList(
                new Student("Иванов Иван Иванович", 5),
                new Student("Петров Пётр Петрович", 4),
                new Student("Сидоров Сидор Сидорович", 3),
                new Student("Козлов Козёл Козлович", 5),
                new Student("Николаев Николай Николаевич", 4),
                new Student("Смирнов Смирный Смирнович", 0),
                new Student("Фёдоров Фёдор Фёдорович", 3)
        );
    }

    /**
     * Tests that analyze returns correct counts for each category.
     */
    @Test
    public void testAnalyzeCountsAreCorrect() {
        GroupStatistics stats = service.analyze(testStudents);
        assertEquals(2, stats.countExcellent(), "Should be 2 excellent students");
        assertEquals(2, stats.countGood(), "Should be 2 good students");
        assertEquals(2, stats.countSatisfactory(), "Should be 2 satisfactory students");
        assertEquals(1, stats.countNotAdmitted(), "Should be 1 not admitted student");
    }

    /**
     * Tests that the average grade excludes not-admitted students.
     */
    @Test
    public void testAverageGradeExcludesNotAdmitted() {
        GroupStatistics stats = service.analyze(testStudents);
        // (5+4+3+5+4+3) / 6 = 24/6 = 4.0
        assertEquals(4.0, stats.getAverageGrade(), 0.001, "Average grade should be 4.0");
    }

    /**
     * Tests that the maximum grade is correctly computed.
     */
    @Test
    public void testMaxGradeIsCorrect() {
        GroupStatistics stats = service.analyze(testStudents);
        assertEquals(5, stats.getMaxGrade());
    }

    /**
     * Tests filtering by grade returns correct students.
     */
    @Test
    public void testFilterByGrade() {
        List<Student> excellentStudents = service.filterByGrade(testStudents, 5);
        assertEquals(2, excellentStudents.size());
        assertTrue(excellentStudents.stream().allMatch(s -> s.getGrade() == 5));
    }

    /**
     * Tests that an empty list throws IllegalArgumentException.
     */
    @Test
    public void testAnalyzeThrowsOnEmptyList() {
        assertThrows(IllegalArgumentException.class,
                () -> service.analyze(Collections.emptyList()));
    }

    /**
     * Tests that null input throws IllegalArgumentException.
     */
    @Test
    public void testAnalyzeThrowsOnNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.analyze(null));
    }

    /**
     * Tests sortByGradeDesc returns students ordered by grade descending.
     */
    @Test
    public void testSortByGradeDesc() {
        List<Student> sorted = service.sortByGradeDesc(testStudents);
        assertEquals(5, sorted.get(0).getGrade());
        assertEquals(0, sorted.get(sorted.size() - 1).getGrade());
    }

    /**
     * Tests that Student.isAdmitted returns false for grade 0.
     */
    @Test
    public void testStudentIsAdmitted() {
        Student admitted = new Student("Тест", 4);
        Student notAdmitted = new Student("Тест2", 0);
        assertTrue(admitted.isAdmitted());
        assertFalse(notAdmitted.isAdmitted());
    }
}
