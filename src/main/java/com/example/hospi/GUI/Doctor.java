package com.example.hospi.GUI;

/**
 * Represents a Doctor user in the hospital management system.
 * Extends the base User class with doctor-specific attributes including
 * specialization, contact information, and professional details.
 */
public class Doctor extends User {
    // Doctor-specific attributes
    private String name;             // Full name of the doctor
    private String specialization;   // Medical specialization area
    private String email;            // Professional email address
    private String phone;            // Contact phone number

    /**
     * Constructs a new Doctor with all required attributes.
     *
     * @param id Unique system identifier
     * @param username Login username
     * @param password Login password
     * @param name Full name of the doctor
     * @param specialization Medical specialty (e.g., "Cardiology")
     * @param email Professional email address
     * @param phone Contact phone number
     */
    public Doctor(int id, String username, String password,
                  String name, String specialization,
                  String email, String phone) {
        // Initialize User superclass with base attributes
        super(id, username, password, "doctor");

        // Initialize Doctor-specific attributes
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
    }

    // ========== PROPERTY ACCESSORS ========== //

    /**
     * @return The doctor's full name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the doctor's name.
     * @param name New full name value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The doctor's medical specialization
     */
    public String getSpecialization() {
        return specialization;
    }

    /**
     * Updates the doctor's specialization.
     * @param specialization New medical specialty
     */
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    /**
     * @return The doctor's professional email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the doctor's email address.
     * @param email New professional email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The doctor's contact phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Updates the doctor's phone number.
     * @param phone New contact number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
}