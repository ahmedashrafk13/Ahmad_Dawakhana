package com.example.hospi.GUI;

/**
 * Abstract base class representing a generic User in the hospital system.
 * Provides common properties and functionality for all user types.
 */
public abstract class User {
    // Protected fields accessible by subclasses
    protected int id;          // Unique identifier for the user
    protected String username; // Login username
    protected String password; // Login password (should be hashed in production)
    protected String role;     // User role (e.g., "doctor", "nurse", "admin")

    /**
     * Constructor for creating a User with just an ID.
     * Typically used when only the identifier is known.
     *
     * @param id The unique identifier for the user
     */
    public User(int id) {
        this.id = id;
    }

    /**
     * Full constructor for creating a User with all basic properties.
     *
     * @param id       The unique identifier for the user
     * @param username The user's login name
     * @param password The user's login password (should be hashed)
     * @param role     The user's role in the system
     */
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }


}