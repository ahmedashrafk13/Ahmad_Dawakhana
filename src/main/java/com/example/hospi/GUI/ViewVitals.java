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
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * JavaFX application that allows doctors to view vital signs of their assigned patients.
 * Features include:
 * - Dropdown list of assigned patients
 * - Table display of patient vitals history
 * - Clean, professional UI with consistent medical styling
 */
public class ViewVitals extends Application {

    // UI Components
    private ComboBox<String> patientDropdown; // Dropdown for patient selection
    private TableView<VitalRecord> vitalsTableView; // Table to display vitals data
    private ObservableList<VitalRecord> vitalsData; // Data model for the table

    // Hardcoded doctor ID (would normally come from login)
    private int doctorId;

    ViewVitals(int doctorId){
        this.doctorId = doctorId;
    }

    /**
     * Main JavaFX start method
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Initialize UI components
        patientDropdown = new ComboBox<>();
        vitalsTableView = new TableView<>();
        vitalsData = FXCollections.observableArrayList();

        // Create and style patient selection label
        Label patientLabel = new Label("Assigned Patients:");
        patientLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64ffda;"); // Teal color

        // Configure patient dropdown
        patientDropdown.setPromptText("Select a patient...");
        patientDropdown.setPrefWidth(300);
        patientDropdown.setStyle("-fx-background-color: #112240; -fx-text-fill: white; -fx-font-size: 14px;");
        patientDropdown.setOnAction(e -> loadPatientVitals()); // Load vitals when selection changes

        // Create and style vitals display label
        Label vitalsLabel = new Label("Patient Vitals:");
        vitalsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64ffda;");

        // Set up table and load initial data
        setupVitalsTable();
        loadAssignedPatients();

        // Create main layout
        VBox leftPanel = new VBox(15, patientLabel, patientDropdown, vitalsLabel, vitalsTableView);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setStyle("-fx-background-color: #0A192F;"); // Dark blue background
        leftPanel.setPrefWidth(1000);

        // Set up and show the scene
        Scene scene = new Scene(leftPanel, 1000, 600);
        primaryStage.setTitle("Doctor - View Patient Vitals");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Configures the vitals table with columns and styling
     */
    private void setupVitalsTable() {
        // Create table columns
        TableColumn<VitalRecord, Integer> idCol = new TableColumn<>("Record ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<VitalRecord, Integer> hrCol = new TableColumn<>("Heart Rate");
        hrCol.setCellValueFactory(new PropertyValueFactory<>("heartRate"));

        TableColumn<VitalRecord, Integer> oxygenCol = new TableColumn<>("Oxygen Level");
        oxygenCol.setCellValueFactory(new PropertyValueFactory<>("oxygenLevel"));

        TableColumn<VitalRecord, Double> tempCol = new TableColumn<>("Temperature (°C)");
        tempCol.setCellValueFactory(new PropertyValueFactory<>("temperature"));

        TableColumn<VitalRecord, String> bpCol = new TableColumn<>("Blood Pressure");
        bpCol.setCellValueFactory(new PropertyValueFactory<>("bloodPressure"));

        TableColumn<VitalRecord, LocalDateTime> recordedCol = new TableColumn<>("Recorded At");
        recordedCol.setCellValueFactory(new PropertyValueFactory<>("recordedAt"));

        // Add columns to table
        vitalsTableView.getColumns().addAll(idCol, hrCol, oxygenCol, tempCol, bpCol, recordedCol);
        vitalsTableView.setItems(vitalsData);
        vitalsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Style the table
        vitalsTableView.setStyle("-fx-background-color: #112240; -fx-text-fill: white;");

        // Custom row factory for hover effects
        vitalsTableView.setRowFactory(tv -> {
            TableRow<VitalRecord> row = new TableRow<>();
            row.setStyle("-fx-background-color: #112240; -fx-text-fill: white;");
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (isNowHovered && !row.isEmpty()) {
                    row.setStyle("-fx-background-color: #1A2F4A; -fx-text-fill: white;");
                } else {
                    row.setStyle("-fx-background-color: #112240; -fx-text-fill: white;");
                }
            });
            return row;
        });

        // Style all columns
        for (TableColumn<?, ?> col : vitalsTableView.getColumns()) {
            col.setStyle("-fx-background-color: #0A192F; -fx-text-fill: #64ffda; -fx-font-weight: bold;");
        }
    }

    /**
     * Loads patients assigned to the current doctor into the dropdown
     */
    private void loadAssignedPatients() {
        String sql ="""
                SELECT DISTINCT p.id, p.name 
                FROM hospital_db.doctorpatientassignment dpa
                JOIN hospital_db.patients p ON dpa.PatientID = p.id
                WHERE dpa.DoctorID = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            // Clear and repopulate dropdown
            patientDropdown.getItems().clear();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                // Format as "ID:123 | Patient Name"
                patientDropdown.getItems().add("ID:" + id + " | " + name);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads vitals data for the currently selected patient
     */
    private void loadPatientVitals() {
        String selected = patientDropdown.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        int patientId = extractPatientId(selected);
        String sql = "SELECT * FROM vitals WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            // Clear and repopulate table data
            vitalsData.clear();
            while (rs.next()) {
                vitalsData.add(new VitalRecord(
                        rs.getInt("id"),
                        rs.getInt("heart_rate"),
                        rs.getInt("oxygen_level"),
                        rs.getDouble("temperature"),
                        rs.getString("blood_pressure"),
                        rs.getTimestamp("recorded_at").toLocalDateTime()
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts patient ID from the dropdown selection string
     * @param entry The dropdown entry in format "ID:123 | Patient Name"
     * @return The extracted patient ID or -1 if parsing fails
     */
    private int extractPatientId(String entry) {
        try {
            return Integer.parseInt(entry.split("\\|")[0].split(":")[1].trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Data model class representing a single vital signs record
     */
    public static class VitalRecord {
        private final IntegerProperty id;
        private final IntegerProperty heartRate;
        private final IntegerProperty oxygenLevel;
        private final DoubleProperty temperature;
        private final StringProperty bloodPressure;
        private final ObjectProperty<LocalDateTime> recordedAt;

        /**
         * Constructor for vital record data model
         * @param id Database record ID
         * @param heartRate Heart rate in BPM
         * @param oxygenLevel Blood oxygen saturation percentage
         * @param temperature Body temperature in Celsius
         * @param bloodPressure Blood pressure as "systolic/diastolic"
         * @param recordedAt Timestamp of when vitals were recorded
         */
        public VitalRecord(int id, int heartRate, int oxygenLevel, double temperature,
                           String bloodPressure, LocalDateTime recordedAt) {
            this.id = new SimpleIntegerProperty(id);
            this.heartRate = new SimpleIntegerProperty(heartRate);
            this.oxygenLevel = new SimpleIntegerProperty(oxygenLevel);
            this.temperature = new SimpleDoubleProperty(temperature);
            this.bloodPressure = new SimpleStringProperty(bloodPressure);
            this.recordedAt = new SimpleObjectProperty<>(recordedAt);
        }

        // Property getters for table column bindings
        public int getId() { return id.get(); }
        public int getHeartRate() { return heartRate.get(); }
        public int getOxygenLevel() { return oxygenLevel.get(); }
        public double getTemperature() { return temperature.get(); }
        public String getBloodPressure() { return bloodPressure.get(); }
        public LocalDateTime getRecordedAt() { return recordedAt.get(); }
    }
}
