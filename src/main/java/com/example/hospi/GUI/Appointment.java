package com.example.hospi.GUI;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a medical appointment with properties suitable for JavaFX binding.
 * Tracks appointment details including timing, participants, and status.
 * Provides real-time status checking and property accessors for UI integration.
 */
public class Appointment {
    // Properties for JavaFX binding
    private final IntegerProperty id;            // Unique appointment identifier
    private final IntegerProperty doctorId;      // ID of the assigned doctor
    private final IntegerProperty patientId;     // ID of the patient
    private final ObjectProperty<LocalDate> date; // Appointment date
    private final ObjectProperty<LocalTime> startTime; // Scheduled start time
    private final ObjectProperty<LocalTime> endTime;   // Scheduled end time
    private final StringProperty status;        // Current status (e.g., "Scheduled", "Completed")

    /**
     * Constructs a new Appointment with specified details.
     *
     * @param id Unique appointment identifier
     * @param patientId ID of the patient
     * @param date Scheduled date of appointment
     * @param startTime Scheduled start time
     * @param endTime Scheduled end time
     * @param status Current appointment status
     */
    public Appointment(int id, int patientId, LocalDate date,
                       LocalTime startTime, LocalTime endTime, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.doctorId = new SimpleIntegerProperty(); // Initialized empty (can be set later)
        this.patientId = new SimpleIntegerProperty(patientId);
        this.date = new SimpleObjectProperty<>(date);
        this.startTime = new SimpleObjectProperty<>(startTime);
        this.endTime = new SimpleObjectProperty<>(endTime);
        this.status = new SimpleStringProperty(status);
    }

    // Standard getter methods
    public int getId() { return id.get(); }
    public int getDoctorId() { return doctorId.get(); }
    public int getPatientId() { return patientId.get(); }
    public LocalDate getDate() { return date.get(); }
    public LocalTime getStartTime() { return startTime.get(); }
    public LocalTime getEndTime() { return endTime.get(); }
    public String getStatus() { return status.get(); }

    /**
     * Provides patient ID property for JavaFX binding.
     * @return IntegerProperty containing patient ID
     */
    public IntegerProperty patientIdProperty() {
        return new SimpleIntegerProperty(getPatientId());
    }

    /**
     * Provides formatted date property for UI display.
     * @return StringProperty with date in string format
     */
    public StringProperty dateProperty() {
        return new SimpleStringProperty(getDate().toString());
    }

    /**
     * Provides formatted time range property for UI display.
     * @return StringProperty with "HH:MM - HH:MM" format
     */
    public StringProperty timeProperty() {
        return new SimpleStringProperty(
                getStartTime().toString() + " - " + getEndTime().toString());
    }

    /**
     * Provides direct access to status property for binding.
     * @return The status StringProperty
     */
    public StringProperty statusProperty() {
        return status;
    }

    /**
     * Checks if the appointment is currently active by comparing with system time.
     * @return true if current time is within the appointment's scheduled time window
     */
    public boolean isNow() {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        return date.get().equals(nowDate) &&       // Today is the appointment date
                !nowTime.isBefore(startTime.get()) &&  // Current time is after start
                !nowTime.isAfter(endTime.get());      // Current time is before end
    }
}

// This class integrates with:
// - VideoCallPage.java for real-time video consultation functionality
// - JavaFX UI components through property binding
// - Appointment scheduling systems through the status property