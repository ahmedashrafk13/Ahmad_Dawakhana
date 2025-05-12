package com.example.hospi.GUI;

/**
 * The Patient class represents a patient user in the hospital management system.
 * It extends the base User class and adds patient-specific attributes and functionality.
 */
public class Patient extends User {
    // Patient-specific attributes
    private String name;     // Full name of the patient
    private String gender;   // Gender of the patient
    private String dob;      // Date of birth (format can be YYYY-MM-DD or other)
    private String phone;    // Contact phone number
    private String address;  // Physical address

    /**
     * Constructs a new Patient instance with all required information.
     *
     * @param id       Unique identifier for the patient
     * @param username Login username for the patient
     * @param password Login password for the patient
     * @param name     Full name of the patient
     * @param gender   Gender of the patient
     * @param dob      Date of birth of the patient
     * @param phone    Contact phone number
     * @param address  Physical address
     */
    public Patient(int id, String username, String password, String name,
                   String gender, String dob, String phone, String address) {
        // Initialize the base User class with patient role
        super(id, username, password, "patient");

        // Initialize patient-specific attributes
        this.name = name;
        this.gender = gender;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
    }

    // ================ GETTER AND SETTER METHODS ================ //

    /**
     * Gets the patient's full name
     * @return The patient's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the patient's full name
     * @param name The new name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the patient's gender
     * @return The patient's gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the patient's gender
     * @param gender The new gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Gets the patient's date of birth
     * @return The date of birth as a string
     */
    public String getDob() {
        return dob;
    }

    /**
     * Sets the patient's date of birth
     * @param dob The new date of birth to set
     */
    public void setDob(String dob) {
        this.dob = dob;
    }

    /**
     * Gets the patient's phone number
     * @return The phone number as a string
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the patient's phone number
     * @param phone The new phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the patient's physical address
     * @return The address as a string
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the patient's physical address
     * @param address The new address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
}