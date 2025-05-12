package com.example.hospi.GUI;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Node;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Chart;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * This class generates comprehensive patient reports with various sections including
 * patient information, appointments, prescriptions, feedback, emergency alerts,
 * health trends, and consultation statistics.
 */
public class GenerateReport extends Application {

    // Constants for styling and configuration
    private static final int DOCTOR_ID = 1; // Default doctor ID
    private static final Color TEXT_COLOR = Color.web("#E0F7FA"); // Light cyan for text
    private static final Color ACCENT_COLOR = Color.web("#87CEEB"); // Sky blue for accents
    private static final Color WARNING_COLOR = Color.web("#FFA07A"); // Light salmon for warnings
    private static final String BACKGROUND_COLOR = "#0d1b2a"; // Dark blue background
    private static final String SECTION_BACKGROUND = "#102841"; // Section background color

    @Override
    public void start(Stage primaryStage) {
        generatePatientReportUI(DOCTOR_ID);
    }

    /**
     * Generates the initial UI for selecting a patient to generate a report
     *
     * @param doctorId The ID of the doctor whose patients we want to list
     */
    public static void generatePatientReportUI(int doctorId) {
        Platform.runLater(() -> {
            // Get map of patient names to IDs
            Map<String, Integer> patientMap = getAssignedPatients(doctorId);

            if (patientMap.isEmpty()) {
                showAlert("No patients assigned to you.");
                return;
            }

            // Create a choice dialog for selecting a patient
            ChoiceDialog<String> dialog = new ChoiceDialog<>(patientMap.keySet().iterator().next(), patientMap.keySet());
            dialog.setTitle("Select Patient");
            dialog.setHeaderText("Choose a patient to generate the report:");
            dialog.setContentText("Patient:");

            // Show dialog and handle selection
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(patientName -> {
                int patientId = patientMap.get(patientName);
                showPatientReportScreen(patientId);
            });
        });
    }

    /**
     * Retrieves a map of patient names to IDs assigned to a specific doctor
     *
     * @param doctorId The ID of the doctor
     * @return Map of patient names (with IDs) to patient IDs
     */
    private static Map<String, Integer> getAssignedPatients(int doctorId) {
        Map<String, Integer> patientMap = new LinkedHashMap<>();
        String query = """
                SELECT DISTINCT p.id, p.name 
                FROM hospital_db.doctorpatientassignment dpa
                JOIN hospital_db.patients p ON dpa.PatientID = p.id
                WHERE dpa.DoctorID = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Format: "Patient Name (ID: 123)"
                String nameWithId = rs.getString("name") + " (ID: " + rs.getInt("id") + ")";
                patientMap.put(nameWithId, rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return patientMap;
    }

    /**
     * Creates and displays the full patient report screen
     *
     * @param patientId The ID of the patient to generate the report for
     */
    private static void showPatientReportScreen(int patientId) {
        VBox reportLayout = new VBox(20);
        reportLayout.setPadding(new Insets(20));
        reportLayout.setStyle("-fx-background-color: #0d1b2a;");

        // Add all report sections
        reportLayout.getChildren().add(createSectionTitle("Patient Info"));
        reportLayout.getChildren().addAll(getPatientInfo(patientId));

        reportLayout.getChildren().add(createSectionTitle("Appointments"));
        reportLayout.getChildren().addAll(getAppointments(patientId));

        reportLayout.getChildren().add(createSectionTitle("Prescriptions"));
        reportLayout.getChildren().addAll(getPrescriptions(patientId));

        reportLayout.getChildren().add(createSectionTitle("Doctor Feedback"));
        reportLayout.getChildren().addAll(getFeedback(patientId));

        reportLayout.getChildren().add(createSectionTitle("Emergency Alerts"));
        reportLayout.getChildren().addAll(getEmergencyAlerts(patientId));

        reportLayout.getChildren().add(createSectionTitle("Health Trends"));
        addVitalsChartsToLayout(reportLayout, patientId);

        reportLayout.getChildren().add(createSectionTitle("🧑‍⚕️ Doctor Consultations Breakdown"));
        reportLayout.getChildren().add(createConsultationPieChart(patientId));

        // Add PDF export button
        Button downloadButton = new Button("📄 Download Report as PDF");
        downloadButton.setStyle("-fx-background-color: #00aaff; -fx-text-fill: white; -fx-font-size: 14;");
        downloadButton.setOnAction(e -> exportReportToPDF(reportLayout));
        reportLayout.getChildren().add(downloadButton);

        // Create scrollable container for the report
        ScrollPane scrollPane = new ScrollPane(reportLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0d1b2a;");

        // Display the report window
        Stage reportStage = new Stage();
        reportStage.setTitle("Patient Report");
        reportStage.setScene(new Scene(scrollPane, 900, 700));
        reportStage.show();
    }

    /**
     * Adds vital signs charts to the report layout
     *
     * @param layout    The VBox layout to add charts to
     * @param patientId The ID of the patient
     */
    private static void addVitalsChartsToLayout(VBox layout, int patientId) {
        Map<String, List<VitalDataPoint>> vitalsData = fetchVitals(patientId);
        if (vitalsData.isEmpty()) {
            Text noData = new Text("⚠ No vitals recorded yet.");
            noData.setFill(Color.ORANGERED);
            noData.setFont(Font.font("Segoe UI", 14));
            layout.getChildren().add(noData);
            return;
        }

        // Create a chart for each type of vital sign
        for (Map.Entry<String, List<VitalDataPoint>> entry : vitalsData.entrySet()) {
            AreaChart<String, Number> chart = createVitalChart(entry.getKey(), entry.getValue());
            layout.getChildren().add(chart);
        }
    }

    /**
     * Creates an area chart for a specific vital sign
     *
     * @param vitalName  The name of the vital sign (e.g., "Heart Rate")
     * @param dataPoints List of data points for the chart
     * @return Configured AreaChart object
     */
    private static AreaChart<String, Number> createVitalChart(String vitalName, List<VitalDataPoint> dataPoints) {
        // Set up chart axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel(vitalName);

        // Create the area chart
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle(vitalName + " Over Time");
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);
        chart.setStyle("-fx-background-color: #102841;");

        // Add data series to the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (VitalDataPoint point : dataPoints) {
            String timestamp = point.timestamp.format(formatter);
            XYChart.Data<String, Number> data = new XYChart.Data<>(timestamp, point.value);
            series.getData().add(data);
        }

        chart.getData().add(series);

        // Style the chart (run later to ensure nodes exist)
        Platform.runLater(() -> {
            Node fill = series.getNode().lookup(".chart-series-area-fill");
            Node line = series.getNode().lookup(".chart-series-area-line");
            if (fill != null) fill.setStyle("-fx-fill: rgba(0,170,255,0.3);");
            if (line != null) line.setStyle("-fx-stroke: #00aaff; -fx-stroke-width: 2px;");
        });

        return chart;
    }

    /**
     * Creates a pie chart showing doctor consultation distribution
     *
     * @param patientId The ID of the patient
     * @return Configured PieChart object
     */
    private static PieChart createConsultationPieChart(int patientId) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Doctor Consultations");
        pieChart.setStyle("-fx-background-color: #102841;");
        pieChart.setLabelsVisible(true);

        String query = """
                SELECT d.name AS doctor_name, COUNT(*) AS consultation_count
                FROM hospital_db.doctorpatientassignment dpa
                JOIN hospital_db.doctors d ON d.id = dpa.DoctorID
                WHERE dpa.PatientID = ?
                GROUP BY d.name
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            // Add data slices to the pie chart
            while (rs.next()) {
                String doctorName = rs.getString("doctor_name");
                int count = rs.getInt("consultation_count");
                PieChart.Data slice = new PieChart.Data(doctorName, count);
                pieChart.getData().add(slice);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pieChart;
    }

    // [Rest of the methods would be similarly commented...]


    private static void exportReportToPDF(VBox reportLayout) {
        // Set up file chooser for PDF export
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                // Initialize PDF document components
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                // Process each node in the report layout
                for (Node node : reportLayout.getChildren()) {
                    if (node instanceof Text text) {
                        // Add text content to PDF
                        document.add(new Paragraph(text.getText()).setFontSize(14).setMarginBottom(5));
                    } else if (node instanceof Chart || node instanceof Region) {
                        // Convert JavaFX nodes to images for PDF inclusion
                        WritableImage fxImage = node.snapshot(new SnapshotParameters(), null);

                        // Convert to BufferedImage for PNG export
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(fxImage, null);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "png", outputStream);

                        // Create PDF image from the snapshot
                        ImageData imageData = ImageDataFactory.create(outputStream.toByteArray());
                        Image pdfImage = new Image(imageData);
                        pdfImage.setAutoScale(true); // Ensure image fits in PDF
                        document.add(pdfImage);
                    }
                }

                // Close PDF resources
                document.close();
                pdfDoc.close();
                writer.close();

                showAlert("PDF generated successfully.");
            } catch (Exception e) {
                showAlert("Error generating PDF: " + e.getMessage());
            }
        }
    }

    /**
     * Fetches vital signs data for a patient from the database
     *
     * @param patientId The ID of the patient
     * @return Map of vital sign names to their data points over time
     */
    private static Map<String, List<VitalDataPoint>> fetchVitals(int patientId) {
        Map<String, List<VitalDataPoint>> vitalsMap = new HashMap<>();
        String query = "SELECT * FROM hospital_db.vitals WHERE patient_id = ? ORDER BY recorded_at ASC";

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
                        // Special handling for blood pressure (contains systolic/diastolic)
                        String bp = rs.getString(vital);
                        if (bp != null && bp.contains("/")) {
                            try {
                                String[] parts = bp.split("/");
                                int systolic = Integer.parseInt(parts[0].trim());
                                int diastolic = Integer.parseInt(parts[1].trim());
                                vitalsMap.computeIfAbsent("Blood Pressure", k -> new ArrayList<>())
                                        .add(new VitalDataPoint(systolic, recordedAt.toLocalDateTime(), diastolic));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else {
                        // Standard numeric vital signs
                        int value = rs.getInt(vital);
                        if (!rs.wasNull()) {
                            // Format column name (e.g., "heart_rate" -> "Heart Rate")
                            String formatted = Arrays.stream(vital.split("_"))
                                    .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                                    .reduce((a, b) -> a + " " + b).orElse(vital);
                            vitalsMap.computeIfAbsent(formatted, k -> new ArrayList<>())
                                    .add(new VitalDataPoint(value, recordedAt.toLocalDateTime()));
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vitalsMap;
    }

    /**
     * Retrieves basic patient information from the database
     *
     * @param patientId The ID of the patient
     * @return List of Text nodes containing patient information
     */
    private static List<Text> getPatientInfo(int patientId) {
        List<Text> info = new ArrayList<>();
        String query = "SELECT * FROM hospital_db.patients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Add all patient information fields
                info.add(createInfoText("Name: " + rs.getString("name")));
                info.add(createInfoText("Gender: " + rs.getString("gender")));
                info.add(createInfoText("DOB: " + rs.getDate("dob")));
                info.add(createInfoText("Phone: " + rs.getString("phone")));
                info.add(createInfoText("Address: " + rs.getString("address")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * Creates a consistently styled Text node for information display
     *
     * @param content The text content to display
     * @return Styled Text node
     */
    private static Text createInfoText(String content) {
        Text text = new Text(content);
        text.setFill(TEXT_COLOR);
        text.setFont(Font.font("Segoe UI", 14));
        return text;
    }

    /**
     * Retrieves appointment history for a patient
     *
     * @param patientId The ID of the patient
     * @return List of Text nodes containing appointment information
     */
    private static List<Text> getAppointments(int patientId) {
        List<Text> appointments = new ArrayList<>();
        String query = "SELECT * FROM hospital_db.appointments WHERE patient_id = ? ORDER BY appointment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Format appointment information
                appointments.add(createInfoText(
                        rs.getDate("appointment_date") + " | " +
                                rs.getTime("start_time") + " - " + rs.getTime("end_time") +
                                " | Status: " + rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    /**
     * Retrieves prescription history for a patient
     *
     * @param patientId The ID of the patient
     * @return List of Text nodes containing prescription information
     */
    private static List<Text> getPrescriptions(int patientId) {
        List<Text> prescriptions = new ArrayList<>();
        String query = "SELECT * FROM hospital_db.prescriptions WHERE PatientID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Format prescription information
                prescriptions.add(createInfoText(
                        rs.getDate("PrescriptionDate") + ": " +
                                rs.getString("MedicineName") + " | " +
                                rs.getString("Dosage") + " | " +
                                rs.getString("Instructions")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prescriptions;
    }

    /**
     * Retrieves doctor feedback for a patient
     *
     * @param patientId The ID of the patient
     * @return List of Text nodes containing feedback information
     */
    private static List<Text> getFeedback(int patientId) {
        List<Text> feedbackList = new ArrayList<>();
        String query = "SELECT * FROM hospital_db.feedback WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Format feedback information
                feedbackList.add(createInfoText(
                        rs.getTimestamp("created_at") + ": " +
                                rs.getString("feedback_text") + " | Medication: " + rs.getString("medication")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbackList;
    }

    /**
     * Retrieves emergency alerts for a patient
     *
     * @param patientId The ID of the patient
     * @return List of Text nodes containing alert information
     */
    private static List<Text> getEmergencyAlerts(int patientId) {
        List<Text> alerts = new ArrayList<>();
        String query = "SELECT * FROM hospital_db.emergency_alerts WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(new Text(rs.getTimestamp("alert_time") + ": " + rs.getString("alert_type")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    /**
     * Creates a styled section title for the report
     *
     * @param title The title text
     * @return Styled Text node for section heading
     */
    private static Text createSectionTitle(String title) {
        Text section = new Text(title);
        section.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        section.setFill(Color.web("#00aaff")); // Blue accent color
        return section;
    }

    /**
     * Shows an information alert dialog
     *
     * @param message The message to display
     */
    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Patient Report");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}