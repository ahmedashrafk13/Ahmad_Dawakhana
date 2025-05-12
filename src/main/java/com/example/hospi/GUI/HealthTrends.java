package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * HealthTrends application displays graphical representations of patient vital signs over time.
 * It provides tabbed charts for different vital metrics with interactive tooltips.
 */
public class HealthTrends extends Application {

    private int patientId; // The patient ID whose data is being displayed
    private BorderPane mainLayout; // Main application layout container
    private int systolic;
    private int diastolic;

    /**
     * Constructor with specific patient ID
     * @param patientId The ID of the patient to display trends for
     */
    public HealthTrends(int patientId) {
        this.patientId = patientId;
    }

    /** Default constructor for testing purposes */
    public HealthTrends() {
        this(1); // Default to patient ID 1
    }

    /**
     * Main entry point for the JavaFX application
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        initializeMainLayout();
        setupApplicationWindow(primaryStage);
    }

    /**
     * Initializes the main application layout with header and content
     */
    private void initializeMainLayout() {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #0d1b2a;"); // Dark blue background

        // Set up header and content areas
        mainLayout.setTop(buildHeader());
        displayVitalData();
    }

    /**
     * Configures the application window properties
     * @param stage The primary stage to configure
     */
    private void setupApplicationWindow(Stage stage) {
        Scene scene = new Scene(mainLayout, 1000, 750);
        stage.setTitle("Health Trends - Lifeline Remote Hospital");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Builds the header section with hospital name and patient info
     * @return Configured VBox containing header elements
     */
    private VBox buildHeader() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Label hospitalName = new Label("🏥 Lifeline Remote Hospital");
        hospitalName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        hospitalName.setTextFill(Color.web("#00aaff")); // Blue accent color

        Label patientInfo = new Label("Health Trends for Patient ID: " + patientId);
        patientInfo.setFont(Font.font("Segoe UI", 18));
        patientInfo.setTextFill(Color.LIGHTGRAY);

        box.getChildren().addAll(hospitalName, patientInfo);
        return box;
    }

    /**
     * Displays vital data in the main content area, either as charts or no-data message
     */
    private void displayVitalData() {
        Map<String, List<VitalDataPoint>> vitalsData = fetchVitals(patientId);

        if (vitalsData.isEmpty()) {
            showNoDataMessage();
        } else {
            displayVitalTabs(vitalsData);
        }
    }

    /**
     * Shows a message when no vital data is available
     */
    private void showNoDataMessage() {
        Label noData = new Label("⚠ No vitals recorded yet.");
        noData.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 18));
        noData.setTextFill(Color.web("#e74c3c")); // Red color for warning

        VBox centerBox = new VBox(noData);
        centerBox.setAlignment(Pos.CENTER);
        mainLayout.setCenter(centerBox);
    }

    /**
     * Creates a tabbed interface for different vital signs
     * @param vitalsData Map of vital sign names to their data points
     */
    private void displayVitalTabs(Map<String, List<VitalDataPoint>> vitalsData) {
        TabPane tabPane = new TabPane();
        tabPane.setTabMinWidth(120);
        tabPane.setStyle("-fx-background-color: #102841; -fx-border-color: #00aaff; -fx-border-radius: 8px;");

        // Create a tab for each vital sign type
        for (String vital : vitalsData.keySet()) {
            Tab tab = new Tab(vital, createVitalChart(vital, vitalsData.get(vital)));
            tab.setClosable(false);
            tabPane.getTabs().add(tab);
        }

        VBox centerBox = new VBox(tabPane);
        centerBox.setPadding(new Insets(10));
        mainLayout.setCenter(centerBox);
    }

    /**
     * Creates an area chart for a specific vital sign
     * @param vitalName Name of the vital sign (e.g., "Heart Rate")
     * @param dataPoints List of data points to display
     * @return Configured AreaChart visualization
     */
    private AreaChart<String, Number> createVitalChart(String vitalName, List<VitalDataPoint> dataPoints) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel(vitalName);
        xAxis.setTickLabelFill(Color.LIGHTGRAY);
        yAxis.setTickLabelFill(Color.LIGHTGRAY);

        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle(vitalName + " Over Time");
        chart.setLegendVisible(true);
        chart.setStyle("-fx-background-color: #102841; -fx-border-color: #00aaff; -fx-border-radius: 6px;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        if (vitalName.equals("Blood Pressure")) {
            XYChart.Series<String, Number> systolicSeries = new XYChart.Series<>();
            systolicSeries.setName("Systolic");

            XYChart.Series<String, Number> diastolicSeries = new XYChart.Series<>();
            diastolicSeries.setName("Diastolic");

            for (VitalDataPoint point : dataPoints) {
                String timestamp = point.timestamp.format(formatter);

                XYChart.Data<String, Number> systolicData = new XYChart.Data<>(timestamp, point.value);
                XYChart.Data<String, Number> diastolicData = new XYChart.Data<>(timestamp, point.diastolic);

                systolicSeries.getData().add(systolicData);
                diastolicSeries.getData().add(diastolicData);

                Tooltip systolicTip = new Tooltip("Systolic: " + point.value + "\nTime: " + timestamp);
                Tooltip diastolicTip = new Tooltip("Diastolic: " + point.diastolic + "\nTime: " + timestamp);
                systolicTip.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white;");
                diastolicTip.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white;");

                systolicData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-background-color: #00aaff, white;");
                        Tooltip.install(newNode, systolicTip);
                    }
                });

                diastolicData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-background-color: #ffaa00, white;");
                        Tooltip.install(newNode, diastolicTip);
                    }
                });
            }

            chart.getData().addAll(systolicSeries, diastolicSeries);
        } else {
            XYChart.Series<String, Number> series = new XYChart.Series<>();

            for (VitalDataPoint point : dataPoints) {
                String timestamp = point.timestamp.format(formatter);
                XYChart.Data<String, Number> data = new XYChart.Data<>(timestamp, point.value);
                series.getData().add(data);

                Tooltip tooltip = new Tooltip("Value: " + point.value + "\nTime: " + timestamp);
                tooltip.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white;");
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-background-color: #00aaff, white;");
                        Tooltip.install(newNode, tooltip);
                    }
                });
            }

            chart.getData().add(series);
        }

        // Final styling
        Platform.runLater(() -> {
            for (XYChart.Series<String, Number> series : chart.getData()) {
                Node fill = series.getNode().lookup(".chart-series-area-fill");
                Node line = series.getNode().lookup(".chart-series-area-line");
                if (fill != null) fill.setStyle("-fx-fill: rgba(0,170,255,0.3);");
                if (line != null) line.setStyle("-fx-stroke: #00aaff; -fx-stroke-width: 2px;");
            }
        });

        return chart;
    }


    /**
     * Fetches vital signs data from the database
     * @param patientId ID of the patient to fetch data for
     * @return Map of vital sign names to their data points
     */
    private Map<String, List<VitalDataPoint>> fetchVitals(int patientId) {
        Map<String, List<VitalDataPoint>> vitalsMap = new HashMap<>();
        String query = "SELECT * FROM vitals WHERE patient_id = ? ORDER BY recorded_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            // Identify all vital sign columns (excluding metadata columns)
            List<String> vitalColumns = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String col = meta.getColumnName(i).toLowerCase();
                if (!col.equals("id") && !col.equals("patient_id") && !col.equals("recorded_at")) {
                    vitalColumns.add(col);
                }
            }

            // Process each row of vital signs data
            while (rs.next()) {
                Timestamp recordedAt = rs.getTimestamp("recorded_at");

                for (String vital : vitalColumns) {
                    if (vital.equals("blood_pressure")) {
                        // Special handling for blood pressure (systolic/diastolic)
                        processBloodPressure(rs, vital, recordedAt, vitalsMap);
                    } else {
                        // Standard numeric vital signs
                        processStandardVital(rs, vital, recordedAt, vitalsMap);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vitalsMap;
    }

    /**
     * Processes blood pressure data from the result set
     */
    private void processBloodPressure(ResultSet rs, String vital, Timestamp recordedAt,
                                      Map<String, List<VitalDataPoint>> vitalsMap) throws SQLException {
        String bp = rs.getString(vital);
        if (bp != null && !bp.isEmpty() && bp.contains("/")) {
            String[] parts = bp.split("/");
            try {
                systolic = Integer.parseInt(parts[0].trim());
                diastolic = Integer.parseInt(parts[1].trim());
                vitalsMap.putIfAbsent("Blood Pressure", new ArrayList<>());
                vitalsMap.get("Blood Pressure").add(
                        new VitalDataPoint(systolic, recordedAt.toLocalDateTime(), diastolic));
            } catch (NumberFormatException e) {
                System.err.println("Invalid BP format: " + bp);
            }
        }
    }

    /**
     * Processes standard vital sign data from the result set
     */
    private void processStandardVital(ResultSet rs, String vital, Timestamp recordedAt,
                                      Map<String, List<VitalDataPoint>> vitalsMap) throws SQLException {
        int value = rs.getInt(vital);
        if (!rs.wasNull()) {
            String formatted = formatVitalName(vital);
            vitalsMap.putIfAbsent(formatted, new ArrayList<>());
            vitalsMap.get(formatted).add(
                    new VitalDataPoint(value, recordedAt.toLocalDateTime()));
        }
    }

    /**
     * Formats database column names into display-friendly names
     * @param dbName Database column name (e.g., "heart_rate")
     * @return Formatted display name (e.g., "Heart Rate")
     */
    private String formatVitalName(String dbName) {
        return Arrays.stream(dbName.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .reduce((a, b) -> a + " " + b)
                .orElse(dbName);
    }

    /**
     * Inner class representing a single data point for a vital sign
     */
    private static class VitalDataPoint {
        int value;          // Primary value (systolic for BP)
        int diastolic;      // Diastolic value (for BP only)
        java.time.LocalDateTime timestamp; // When the measurement was taken

        VitalDataPoint(int value, java.time.LocalDateTime timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        VitalDataPoint(int systolic, java.time.LocalDateTime timestamp, int diastolic) {
            this(systolic, timestamp);
            this.diastolic = diastolic;
        }
    }
}
