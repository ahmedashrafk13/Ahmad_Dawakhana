package com.example.hospi.GUI;

import java.time.LocalDateTime;

/**
 * Represents a single data point for patient vital signs.
 * Can store various types of vitals including:
 * - Heart rate
 * - Oxygen level
 * - Blood pressure (with systolic and diastolic values)
 * - Temperature
 * Each data point includes a timestamp for tracking when the measurement was taken.
 */
public class VitalDataPoint {
    // The primary value of the vital measurement
    // For most vitals (heart rate, oxygen level, temperature) this is the only value needed
    // For blood pressure, this represents the systolic value
    public int value;

    // The timestamp when this vital was recorded
    public LocalDateTime timestamp;

    // Optional diastolic value, used specifically for blood pressure measurements
    // Will be 0 for other vital types
    public int diastolic;

    /**
     * Constructor for single-value vital measurements (heart rate, oxygen level, temperature)
     * @param value The measurement value (e.g., 72 for heart rate, 98 for oxygen level)
     * @param timestamp When the measurement was taken
     */
    public VitalDataPoint(int value, LocalDateTime timestamp) {
        this.value = value;
        this.timestamp = timestamp;
        this.diastolic = 0; // Initialize to 0 for non-blood pressure measurements
    }

    /**
     * Specialized constructor for blood pressure measurements
     * @param systolic The systolic blood pressure value
     * @param timestamp When the measurement was taken
     * @param diastolic The diastolic blood pressure value
     */
    public VitalDataPoint(int systolic, LocalDateTime timestamp, int diastolic) {
        this.value = systolic; // Store systolic in the primary value field
        this.timestamp = timestamp;
        this.diastolic = diastolic;
    }
}