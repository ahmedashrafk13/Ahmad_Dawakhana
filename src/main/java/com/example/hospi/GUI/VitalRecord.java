package com.example.hospi.GUI;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Represents a patient's vital signs record for display in a JavaFX TableView.
 * Extends RecursiveTreeObject to support display in JFoenix TreeTableView.
 * Contains properties for all standard vital signs with JavaFX property bindings.
 */
public class VitalRecord extends RecursiveTreeObject<VitalRecord> {

    // Property fields for JavaFX data binding
    private final IntegerProperty id;            // Unique record identifier
    private final IntegerProperty heartRate;     // Heart rate in BPM
    private final IntegerProperty oxygenLevel;   // Blood oxygen saturation (%)
    private final DoubleProperty temperature;    // Body temperature in Celsius
    private final StringProperty bloodPressure;  // Blood pressure as "systolic/diastolic"
    private final ObjectProperty<LocalDateTime> recordedAt; // Timestamp of measurement

    /**
     * Constructs a new VitalRecord with all vital sign measurements.
     *
     * @param id            The unique database record ID
     * @param heartRate     Heart rate in beats per minute
     * @param oxygenLevel   Blood oxygen saturation percentage
     * @param temperature   Body temperature in Celsius
     * @param bloodPressure Blood pressure as "systolic/diastolic" string
     * @param recordedAt    Date and time when vitals were recorded
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

    /**
     * @return The IntegerProperty for the record ID (for JavaFX binding)
     */
    public IntegerProperty idProperty() {
        return id;
    }

    /**
     * @return The IntegerProperty for heart rate (for JavaFX binding)
     */
    public IntegerProperty heartRateProperty() {
        return heartRate;
    }

    /**
     * @return The IntegerProperty for oxygen level (for JavaFX binding)
     */
    public IntegerProperty oxygenLevelProperty() {
        return oxygenLevel;
    }

    /**
     * @return The DoubleProperty for temperature (for JavaFX binding)
     */
    public DoubleProperty temperatureProperty() {
        return temperature;
    }

    /**
     * @return The StringProperty for blood pressure (for JavaFX binding)
     */
    public StringProperty bloodPressureProperty() {
        return bloodPressure;
    }

    /**
     * @return The ObjectProperty for recording timestamp (for JavaFX binding)
     */
    public ObjectProperty<LocalDateTime> recordedAtProperty() {
        return recordedAt;
    }
}