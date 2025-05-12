package com.example.hospi.GUI;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PatientVideoCall {
    private int patientId;
    private Connection conn;

    public PatientVideoCall(int patientId, Connection conn) {
        this.patientId = patientId;
        this.conn = conn;
    }

    // Get available slots for a doctor
    public List<LocalDateTime> getAvailableSlots(int doctorId) throws SQLException {
        List<LocalDateTime> slots = new ArrayList<>();
        String query = """
            SELECT available_date, start_time 
            FROM doctor_availability 
            WHERE doctor_id = ? AND available_date >= CURDATE()
        """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("available_date").toLocalDate();
                    LocalTime time = rs.getTime("start_time").toLocalTime();
                    slots.add(LocalDateTime.of(date, time));
                }
            }
        }
        return slots;
    }

    // Book appointment
    public void bookAppointment(int doctorId, LocalDateTime appointmentTime) throws SQLException {
        String insert = """
            INSERT INTO video_call_appointments (doctor_id, patient_id, appointment_time, status) 
            VALUES (?, ?, ?, 'PENDING')
        """;
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setInt(1, doctorId);
            stmt.setInt(2, patientId);
            stmt.setTimestamp(3, Timestamp.valueOf(appointmentTime));
            stmt.executeUpdate();
            System.out.println("✅ Video call appointment requested.");
        }
    }

    // Main method for launching this test via console
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection();
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your Patient ID: ");
            int patientId = scanner.nextInt();

            System.out.print("Enter Doctor ID to view availability: ");
            int doctorId = scanner.nextInt();

            PatientVideoCall pvc = new PatientVideoCall(patientId, conn);
            List<LocalDateTime> slots = pvc.getAvailableSlots(doctorId);

            if (slots.isEmpty()) {
                System.out.println("❌ No available slots for the doctor.");
                return;
            }

            System.out.println("Available Slots:");
            for (int i = 0; i < slots.size(); i++) {
                System.out.println((i + 1) + ". " + slots.get(i));
            }

            System.out.print("Select slot number to book: ");
            int choice = scanner.nextInt();
            if (choice < 1 || choice > slots.size()) {
                System.out.println("❌ Invalid selection.");
                return;
            }

            LocalDateTime selectedSlot = slots.get(choice - 1);
            pvc.bookAppointment(doctorId, selectedSlot);

        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }
}
