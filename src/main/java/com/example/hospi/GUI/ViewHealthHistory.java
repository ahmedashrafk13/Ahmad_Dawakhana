package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JavaFX application for viewing a patient's health/vitals history.
 * Displays vital signs in a table format with timestamp information.
 */
public class ViewHealthHistory extends Application {

    private int patientId; // Stores the patient ID whose history is being viewed

    /**
     * Inner class representing a single vital signs record.
     * Used as the data model for table rows.
     */
    public static class VitalRecord {
        private final SimpleIntegerProperty heartRate;
        private final SimpleIntegerProperty oxygenLevel;
        private final SimpleStringProperty temperature;
        private final SimpleStringProperty bloodPressure;
        private final SimpleObjectProperty<LocalDateTime> recordedAt;

        /**
         * Constructor for vital record data model
         * @param heartRate Heart rate measurement (bpm)
         * @param oxygenLevel Blood oxygen saturation (%)
         * @param temperature Body temperature (as string)
         * @param bloodPressure Blood pressure (format: "systolic/diastolic")
         * @param recordedAt Timestamp of when vitals were recorded
         */
        public VitalRecord(int heartRate, int oxygenLevel, String temperature, String bloodPressure, LocalDateTime recordedAt) {
            this.heartRate = new SimpleIntegerProperty(heartRate);
            this.oxygenLevel = new SimpleIntegerProperty(oxygenLevel);
            this.temperature = new SimpleStringProperty(temperature);
            this.bloodPressure = new SimpleStringProperty(bloodPressure);
            this.recordedAt = new SimpleObjectProperty<>(recordedAt);
        }

        // Property getters for table column bindings
        public int getHeartRate() { return heartRate.get(); }
        public int getOxygenLevel() { return oxygenLevel.get(); }
        public String getTemperature() { return temperature.get(); }
        public String getBloodPressure() { return bloodPressure.get(); }
        public LocalDateTime getRecordedAt() { return recordedAt.get(); }
    }

    /**
     * Default constructor - creates view with default patient ID 1
     */
    public ViewHealthHistory() {
        this.patientId = -1; // Will be set to 1 in start()
    }

    /**
     * Constructor with specific patient ID
     * @param patientId ID of patient whose history to view
     */
    public ViewHealthHistory(int patientId) {
        this.patientId = patientId;
    }

    /**
     * Main JavaFX application start method
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        if (patientId == -1) patientId = 1; // Default to patient 1 if not set
        String patientName = fetchPatientName(patientId);

        // Create title label with patient info
        Label title = new Label("Vitals History of " + patientName + " (ID: " + patientId + ")");
        title.setFont(Font.font("Arial", 24));
        title.setTextFill(Color.web("#00BFFF"));
        title.setStyle("-fx-font-weight: bold; -fx-underline: true;");

        // Create table to display vitals history
        TableView<VitalRecord> vitalsTable = new TableView<>();
        vitalsTable.setItems(fetchVitals(patientId)); // Load data from database
        vitalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        styleVitalsTable(vitalsTable); // Apply custom styling

        // Add columns to table
        vitalsTable.getColumns().addAll(
                createTableColumn("Heart Rate", "heartRate"),
                createTableColumn("Oxygen Level", "oxygenLevel"),
                createTableColumn("Temperature", "temperature"),
                createTableColumn("Blood Pressure", "bloodPressure"),
                createFormattedDateColumn("Recorded At")
        );

        // Create back button to return to dashboard
        Button backButton = new Button("Back to Dashboard");
        backButton.setPrefWidth(200);
        backButton.setPrefHeight(40);
        backButton.setStyle("-fx-background-color: #00BFFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        // Hover effects
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #009ACD; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #00BFFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
        backButton.setOnAction(e -> {
            PatientDashboard dashboard = new PatientDashboard(patientId, patientName);
            dashboard.start(primaryStage);
        });

        // Set up main layout
        VBox layout = new VBox(25, title, vitalsTable, backButton);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #0A192F;"); // Dark blue background

        // Configure and show the stage
        Scene scene = new Scene(layout, 900, 550);
        primaryStage.setTitle("Patient Vitals History");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates a standard table column with property binding
     * @param title Column header text
     * @param propertyName Name of the property to bind to
     * @return Configured TableColumn instance
     */
    private TableColumn<VitalRecord, ?> createTableColumn(String title, String propertyName) {
        TableColumn<VitalRecord, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-background-color: #0A192F; -fx-text-fill: #00BFFF; -fx-font-weight: bold; -fx-font-size: 13px;");
        return column;
    }

    /**
     * Creates a formatted date column with custom date formatting
     * @param title Column header text
     * @return Configured TableColumn instance for dates
     */
    private TableColumn<VitalRecord, String> createFormattedDateColumn(String title) {
        TableColumn<VitalRecord, String> column = new TableColumn<>(title);
        // Format the LocalDateTime as a readable string
        column.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRecordedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        column.setStyle("-fx-background-color: #0A192F; -fx-text-fill: #00BFFF; -fx-font-weight: bold; -fx-font-size: 13px;");
        return column;
    }

    /**
     * Fetches vital signs records from the database for a specific patient
     * @param patientId ID of patient to fetch records for
     * @return ObservableList of VitalRecord objects for the table
     */
    private ObservableList<VitalRecord> fetchVitals(int patientId) {
        ObservableList<VitalRecord> records = FXCollections.observableArrayList();
        String query = "SELECT heart_rate, oxygen_level, temperature, blood_pressure, recorded_at FROM vitals WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            // Convert each database row to a VitalRecord object
            while (rs.next()) {
                records.add(new VitalRecord(
                        rs.getInt("heart_rate"),
                        rs.getInt("oxygen_level"),
                        rs.getString("temperature"),
                        rs.getString("blood_pressure"),
                        rs.getTimestamp("recorded_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Applies custom styling to the vitals table
     * @param table TableView to style
     */
    private void styleVitalsTable(TableView<VitalRecord> table) {
        table.setStyle("-fx-background-color: #112240;"); // Dark blue background

        // Custom row factory for hover effects
        table.setRowFactory(tv -> {
            TableRow<VitalRecord> row = new TableRow<>();
            row.setStyle("-fx-background-color: #112240; -fx-text-fill: #E0F7FA;");

            // Change background on hover
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (!row.isEmpty()) {
                    row.setStyle(isNowHovered
                            ? "-fx-background-color: #1A2F4A; -fx-text-fill: #E0F7FA;"
                            : "-fx-background-color: #112240; -fx-text-fill: #E0F7FA;");
                }
            });
            return row;
        });

        // Style all columns
        table.getColumns().forEach(col ->
                col.setStyle("-fx-background-color: #0A192F; -fx-text-fill: #00BFFF; -fx-font-weight: bold; -fx-font-size: 13px;")
        );
    }

    /**
     * Fetches a patient's name from the database
     * @param patientId ID of patient to lookup
     * @return Patient's name or "Patient" if not found
     */
    private String fetchPatientName(int patientId) {
        String query = "SELECT name FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("name");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Patient"; // Default if name not found
    }

    public static void main(String[] args) {
        launch(args);
    }
}