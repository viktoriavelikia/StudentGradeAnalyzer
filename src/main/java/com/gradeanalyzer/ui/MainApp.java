package com.gradeanalyzer.ui;

import com.gradeanalyzer.model.GroupStatistics;
import com.gradeanalyzer.model.Student;
import com.gradeanalyzer.service.ExcelReaderService;
import com.gradeanalyzer.service.ExcelWriterService;
import com.gradeanalyzer.service.GradeAnalysisService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Main JavaFX application window for the Student Grade Analyzer.
 * Provides a graphical interface to select input files, analyze grades,
 * and save the results to an output Excel file.
 */
public class MainApp extends Application {

    private static final Logger logger = LogManager.getLogger(MainApp.class);

    private final ExcelReaderService readerService = new ExcelReaderService();
    private final GradeAnalysisService analysisService = new GradeAnalysisService();
    private final ExcelWriterService writerService = new ExcelWriterService();

    private Label inputFileLabel;
    private Label outputFileLabel;
    private TextArea resultArea;
    private String selectedInputPath;
    private String selectedOutputPath;
    // ДОБАВИТЬ ЭТО ПОЛЕ:
    private boolean isAnalyzing = false;
    private Button analyzeBtn;  // Нужно вынести кнопку в поле класса
    /**
     * Entry point for the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        logger.info("Application starting");
        launch(args);
    }

    /**
     * Initializes and shows the main application window.
     *
     * @param primaryStage the primary JavaFX stage
     */
    @Override
    public void start(Stage primaryStage) {
        logger.info("Initializing UI");
        primaryStage.setTitle("Анализ успеваемости студентов");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f4f4;");

        // --- Header ---
        Label title = new Label("Анализ успеваемости по отчётной ведомости");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // --- Input file selection ---
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        inputFileLabel = new Label("Файл не выбран");
        inputFileLabel.setStyle("-fx-text-fill: #666;");
        Button chooseInputBtn = new Button("Выбрать Excel файл (вход)");
        chooseInputBtn.setOnAction(e -> chooseInputFile(primaryStage));
        inputBox.getChildren().addAll(chooseInputBtn, inputFileLabel);

        // --- Output file selection ---
        HBox outputBox = new HBox(10);
        outputBox.setAlignment(Pos.CENTER_LEFT);
        outputFileLabel = new Label("Файл не выбран");
        outputFileLabel.setStyle("-fx-text-fill: #666;");
        Button chooseOutputBtn = new Button("Выбрать путь (выход)");
        chooseOutputBtn.setOnAction(e -> chooseOutputFile(primaryStage));
        outputBox.getChildren().addAll(chooseOutputBtn, outputFileLabel);

        // --- Analyze button ---
        analyzeBtn = new Button("Анализировать и сохранить");
        analyzeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setOnAction(e -> runAnalysis());

        // --- Results area ---
        Label resultsLabel = new Label("Результаты:");
        resultsLabel.setStyle("-fx-font-weight: bold;");
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(350);
        resultArea.setStyle("-fx-font-family: monospace;");

        // --- Exit button ---
        Button exitBtn = new Button("Выход");
        exitBtn.setOnAction(e -> Platform.exit());

        root.getChildren().addAll(title, new Separator(),
                inputBox, outputBox, analyzeBtn,
                resultsLabel, resultArea, exitBtn);

        Scene scene = new Scene(root, 700, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        logger.info("UI shown");
    }

    /**
     * Opens a file chooser dialog to select the input Excel file.
     *
     * @param stage the parent stage
     */
    private void chooseInputFile(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите Excel файл с оценками");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            selectedInputPath = file.getAbsolutePath();
            inputFileLabel.setText(file.getName());
            logger.info("Input file selected: {}", selectedInputPath);
        }
    }

    /**
     * Opens a file save dialog to choose the output Excel file path.
     *
     * @param stage the parent stage
     */
    private void chooseOutputFile(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Сохранить результаты как...");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("результаты.xlsx");
        File file = fc.showSaveDialog(stage);
        if (file != null) {
            selectedOutputPath = file.getAbsolutePath();
            if (!selectedOutputPath.endsWith(".xlsx")) {
                selectedOutputPath += ".xlsx";
            }
            outputFileLabel.setText(file.getName());
            logger.info("Output file selected: {}", selectedOutputPath);
        }
    }

    /**
     * Runs the full analysis pipeline: read → analyze → write → display.
     * Handles errors and displays user-friendly messages.
     */
    private void runAnalysis() {
        // Проверка на уже выполняющийся анализ
        if (isAnalyzing) {
            logger.warn("Analysis already in progress, ignoring request");
            return;
        }

        resultArea.clear();

        if (selectedInputPath == null) {
            showAlert("Ошибка", "Выберите входной Excel файл!");
            return;
        }
        if (selectedOutputPath == null) {
            showAlert("Ошибка", "Выберите путь для сохранения результата!");
            return;
        }

        // Блокируем кнопку
        isAnalyzing = true;
        analyzeBtn.setDisable(true);
        analyzeBtn.setText("Анализ...");

        try {
            logger.info("Starting analysis pipeline");

            List<Student> students = readerService.readStudents(selectedInputPath);
            if (students.isEmpty()) {
                showAlert("Ошибка", "В файле не найдено студентов.");
                return;
            }

            GroupStatistics stats = analysisService.analyze(students);
            writerService.writeResults(stats, selectedOutputPath);

            displayResults(stats);
            logger.info("Analysis pipeline completed successfully");

        } catch (Exception ex) {
            logger.error("Analysis failed: {}", ex.getMessage(), ex);
            showAlert("Ошибка", "Ошибка при анализе:\n" + ex.getMessage());
        } finally {
            // Разблокируем кнопку в любом случае
            isAnalyzing = false;
            analyzeBtn.setDisable(false);
            analyzeBtn.setText("Анализировать и сохранить");
        }
    }

    /**
     * Formats and displays the analysis results in the text area.
     *
     * @param stats the computed group statistics
     */
    private void displayResults(GroupStatistics stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== РЕЗУЛЬТАТЫ АНАЛИЗА ==========\n\n");
        sb.append(String.format("%-25s %d\n", "Отличников (5):", stats.countExcellent()));
        sb.append(String.format("%-25s %d\n", "Хорошистов (4):", stats.countGood()));
        sb.append(String.format("%-25s %d\n", "Троечников (3):", stats.countSatisfactory()));
        sb.append(String.format("%-25s %d\n", "Не допущено (0):", stats.countNotAdmitted()));
        sb.append(String.format("%-25s %.2f\n", "Средний балл:", stats.getAverageGrade()));
        sb.append(String.format("%-25s %d\n\n", "Максимальная оценка:", stats.getMaxGrade()));

        appendStudentList(sb, "Отличники (5):", stats.getExcellentStudents());
        appendStudentList(sb, "Хорошисты (4):", stats.getGoodStudents());
        appendStudentList(sb, "Троечники (3):", stats.getSatisfactoryStudents());
        appendStudentList(sb, "Не допущены:", stats.getNotAdmittedStudents());

        sb.append("\n✓ Результаты сохранены в: ").append(selectedOutputPath);
        resultArea.setText(sb.toString());
    }

    /**
     * Appends a student list block to the StringBuilder.
     */
    private void appendStudentList(StringBuilder sb, String header, List<Student> students) {
        sb.append(header).append("\n");
        if (students.isEmpty()) {
            sb.append("  —\n");
        } else {
            students.forEach(s -> sb.append("  • ").append(s.getFullName())
                    .append(" (").append(s.getGrade()).append(")\n"));
        }
        sb.append("\n");
    }

    /**
     * Shows an error alert dialog.
     *
     * @param title   dialog title
     * @param message message content
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
