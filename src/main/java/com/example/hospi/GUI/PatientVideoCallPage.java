package com.example.hospi.GUI;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.*;
import java.util.*;

public class PatientVideoCallPage {

    private final int patientId;
    private final Connection conn;
    private final ComboBox<String> doctorComboBox = new ComboBox<>();
    private final ComboBox<LocalDate> dayComboBox = new ComboBox<>();
    private final ComboBox<LocalTime> timeComboBox = new ComboBox<>();
    private final Label statusLabel = new Label();
    private final Map<String, Integer> doctorNameToId = new HashMap<>();
    private final Map<LocalDate, TimeRange> doctorAvailabilityByDate = new HashMap<>();

    public PatientVideoCallPage(int patientId) throws SQLException {
        this.patientId = patientId;
        this.conn = DatabaseConnection.getConnection();
    }

    public void show() {
        Stage stage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1e2f;");

        Label titleLabel = new Label("📞 Book Video Call Appointment");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        Label doctorLabel = new Label("Select Assigned Doctor:");
        doctorLabel.setStyle("-fx-text-fill: white;");

        doctorComboBox.setPromptText("Select Doctor");
        loadAssignedDoctors();

        Button loadDaysButton = new Button("Load Available Days");
        loadDaysButton.setOnAction(e -> loadAvailableDays());

        Label dayLabel = new Label("Select Day:");
        dayLabel.setStyle("-fx-text-fill: white;");

        dayComboBox.setPromptText("Select Day");
        dayComboBox.setOnAction(e -> loadAvailableTimes());

        Label timeLabel = new Label("Select Time:");
        timeLabel.setStyle("-fx-text-fill: white;");
        timeComboBox.setPromptText("Select 1-hour Slot");

        Button bookBtn = new Button("Book Appointment");
        bookBtn.setOnAction(e -> bookAppointment());

        statusLabel.setStyle("-fx-text-fill: lightgreen;");

        root.getChildren().addAll(
                titleLabel,
                doctorLabel, doctorComboBox,
                loadDaysButton,
                dayLabel, dayComboBox,
                timeLabel, timeComboBox,
                bookBtn,
                statusLabel
        );

        Scene scene = new Scene(root, 420, 480);
        stage.setTitle("Lifeline Remote Hospital - Video Call");
        stage.setScene(scene);
        stage.show();
    }

    private void loadAssignedDoctors() {
        doctorComboBox.getItems().clear();
        doctorNameToId.clear();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT d.id, d.name FROM doctorpatientassignment a " +
                        "JOIN doctors d ON a.DoctorID = d.id " +
                        "WHERE a.PatientID = ?")) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                doctorNameToId.put(name, id);
                doctorComboBox.getItems().add(name);
            }
        } catch (SQLException e) {
            statusLabel.setText("❌ Error loading doctors.");
            e.printStackTrace();
        }
    }

    private void loadAvailableDays() {
        dayComboBox.getItems().clear();
        doctorAvailabilityByDate.clear();
        String selectedDoctor = doctorComboBox.getValue();
        if (selectedDoctor == null) {
            statusLabel.setText("❌ Please select a doctor.");
            return;
        }

        int doctorId = doctorNameToId.get(selectedDoctor);

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT available_date, start_time, end_time FROM doctor_availability " +
                        "WHERE doctor_id = ? AND available_date >= CURDATE()")) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            List<LocalDate> availableDays = new ArrayList<>();

            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("available_date");
                String startStr = rs.getString("start_time");
                String endStr = rs.getString("end_time");

                if (sqlDate != null && startStr != null && endStr != null) {
                    LocalDate date = sqlDate.toLocalDate();
                    LocalTime start = LocalTime.parse(startStr);
                    LocalTime end = LocalTime.parse(endStr);
                    doctorAvailabilityByDate.put(date, new TimeRange(start, end));
                    availableDays.add(date);
                } else {
                    System.out.println("⚠️ Skipped: Null time or date for doctor " + doctorId);
                }
            }

            if (availableDays.isEmpty()) {
                statusLabel.setText("❌ No available days.");
            } else {
                dayComboBox.setItems(FXCollections.observableArrayList(availableDays));
                statusLabel.setText("✅ Days loaded.");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Error loading days: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAvailableTimes() {
        timeComboBox.getItems().clear();

        String selectedDoctor = doctorComboBox.getValue();
        LocalDate selectedDate = dayComboBox.getValue();

        if (selectedDoctor == null || selectedDate == null) {
            statusLabel.setText("❌ Select doctor and date.");
            return;
        }

        int doctorId = doctorNameToId.get(selectedDoctor);
        TimeRange range = doctorAvailabilityByDate.get(selectedDate);
        if (range == null) {
            statusLabel.setText("❌ No time range available.");
            return;
        }

        List<LocalTime> availableTimes = new ArrayList<>();
        LocalTime current = range.start;
        while (!current.plusHours(1).isAfter(range.end)) {
            LocalDateTime startDateTime = LocalDateTime.of(selectedDate, current);
            try {
                if (isSlotFree(doctorId, startDateTime)) {
                    availableTimes.add(current);
                }
            } catch (SQLException e) {
                statusLabel.setText("❌ Slot check error: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            current = current.plusMinutes(30); // Optional 30-min granularity
        }

        if (availableTimes.isEmpty()) {
            statusLabel.setText("❌ No available time slots.");
        } else {
            timeComboBox.setItems(FXCollections.observableArrayList(availableTimes));
            statusLabel.setText("✅ Time slots loaded.");
        }
    }

    private boolean isSlotFree(int doctorId, LocalDateTime dateTime) throws SQLException {
        Timestamp start = Timestamp.valueOf(dateTime);
        Timestamp end = Timestamp.valueOf(dateTime.plusHours(1));

        String query = """
            SELECT 1 FROM appointments 
            WHERE doctor_id = ? AND status != 'cancelled' AND 
                  appointment_date = ? AND 
                  (start_time < ? AND end_time > ?)
            UNION
            SELECT 1 FROM video_call_appointments
            WHERE doctor_id = ? AND 
                  appointment_time < ? AND 
                  DATE_ADD(appointment_time, INTERVAL 1 HOUR) > ? AND 
                  status != 'cancelled'
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            stmt.setDate(2, java.sql.Date.valueOf(dateTime.toLocalDate()));
            stmt.setTime(3, Time.valueOf(dateTime.plusHours(1).toLocalTime()));
            stmt.setTime(4, Time.valueOf(dateTime.toLocalTime()));
            stmt.setInt(5, doctorId);
            stmt.setTimestamp(6, end);
            stmt.setTimestamp(7, start);

            ResultSet rs = stmt.executeQuery();
            return !rs.next(); // true if no conflict
        }
    }

    private void bookAppointment() {
        String selectedDoctor = doctorComboBox.getValue();
        LocalDate selectedDate = dayComboBox.getValue();
        LocalTime selectedTime = timeComboBox.getValue();

        if (selectedDoctor == null || selectedDate == null || selectedTime == null) {
            statusLabel.setText("❌ Please complete all selections.");
            return;
        }

        int doctorId = doctorNameToId.get(selectedDoctor);
        LocalDateTime appointmentTime = LocalDateTime.of(selectedDate, selectedTime);

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO video_call_appointments (doctor_id, patient_id, appointment_time, status, created_at) " +
                        "VALUES (?, ?, ?, 'pending', NOW())")) {
            stmt.setInt(1, doctorId);
            stmt.setInt(2, patientId);
            stmt.setTimestamp(3, Timestamp.valueOf(appointmentTime));
            stmt.executeUpdate();

            statusLabel.setText("✅ Video call appointment requested!");
        } catch (SQLException e) {
            statusLabel.setText("❌ Booking failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class TimeRange {
        LocalTime start;
        LocalTime end;

        TimeRange(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
