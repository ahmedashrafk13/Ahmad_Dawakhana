package com.example.hospi.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AddUserDialog extends JDialog {

    private int adminId;

    public AddUserDialog(JFrame parent, UserService userService, DefaultTableModel userModel, int adminId) {
        super(parent, "➕ Add New User", true);
        this.adminId = adminId;

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        Color panelGray = new Color(37, 47, 63);
        Color textLight = new Color(230, 230, 230);
        Color accent = new Color(0, 188, 212);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(panelGray);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField usernameField = createTextField(panelGray, textLight);
        JTextField passwordField = createTextField(panelGray, textLight);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Doctor", "Patient"});
        JTextField nameField = createTextField(panelGray, textLight);
        JTextField emailField = createTextField(panelGray, textLight);
        JTextField phoneField = createTextField(panelGray, textLight);
        JTextField specializationField = createTextField(panelGray, textLight);
        JTextField genderField = createTextField(panelGray, textLight);
        JTextField dobField = createTextField(panelGray, textLight);

        formPanel.add(createLabel("Username:", textLight));
        formPanel.add(usernameField);
        formPanel.add(createLabel("Password:", textLight));
        formPanel.add(passwordField);
        formPanel.add(createLabel("Role:", textLight));
        formPanel.add(roleBox);
        formPanel.add(createLabel("Name:", textLight));
        formPanel.add(nameField);
        formPanel.add(createLabel("Email:", textLight));
        formPanel.add(emailField);
        formPanel.add(createLabel("Phone:", textLight));
        formPanel.add(phoneField);
        formPanel.add(createLabel("Specialization (Doctor only):", textLight));
        formPanel.add(specializationField);
        formPanel.add(createLabel("Gender (Patient only):", textLight));
        formPanel.add(genderField);
        formPanel.add(createLabel("DOB (YYYY-MM-DD):", textLight));
        formPanel.add(dobField);

        JButton submitBtn = new JButton("✅ Submit");
        styleButton(submitBtn, accent);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(panelGray);
        buttonPanel.add(submitBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        submitBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleBox.getSelectedItem().toString();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String specialization = specializationField.getText().trim();
            String gender = genderField.getText().trim();
            String dob = dobField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }

            try {
                boolean success = addUser(username, password, role, name, email, phone, specialization, gender, dob);
                if (success) {
                    JOptionPane.showMessageDialog(this, role + " added successfully.");
                    userService.loadUsers(userModel);
                    dispose();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }

    private boolean addUser(String username, String password, String role,
                            String name, String email, String phone,
                            String specialization, String gender, String dob) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String insertUserSQL = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.setString(3, role);
                userStmt.executeUpdate();

                ResultSet keys = userStmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("Failed to retrieve user ID.");
                int userId = keys.getInt(1);

                if (role.equals("Doctor")) {
                    if (specialization.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Specialization is required for doctors.");
                        conn.rollback();
                        return false;
                    }
                    insertDoctor(conn, userId, name, specialization, phone, email);
                } else if (role.equals("Patient")) {
                    if (gender.isEmpty() || dob.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Gender and DOB are required for patients.");
                        conn.rollback();
                        return false;
                    }
                    insertPatient(conn, userId, name, gender, dob, phone, email);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid role.");
                    conn.rollback();
                    return false;
                }

                // ✅ Log using dynamically fetched admin username
                String adminUsername = getAdminUsername(conn, this.adminId);
                logAdminAction(conn, adminUsername, "Add User", "Added " + role + " with username: " + username);

                conn.commit();
                return true;
            }
        }
    }

    private String getAdminUsername(Connection conn, int adminId) throws SQLException {
        String query = "SELECT username FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, adminId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                } else {
                    throw new SQLException("Admin username not found for ID: " + adminId);
                }
            }
        }
    }

    private void insertDoctor(Connection conn, int userId, String name,
                              String specialization, String phone, String email) throws SQLException {
        String insertDoctor = "INSERT INTO doctors (user_id, name, specialization, phone, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertDoctor)) {
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setString(3, specialization);
            stmt.setString(4, phone);
            stmt.setString(5, email);
            stmt.executeUpdate();
        }
    }

    private void insertPatient(Connection conn, int userId, String name,
                               String gender, String dob, String phone, String address) throws SQLException {
        Date sqlDob;
        try {
            sqlDob = Date.valueOf(dob);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            conn.rollback();
            throw ex;
        }
        String insertPatient = "INSERT INTO patients (user_id, name, gender, dob, phone, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertPatient)) {
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setString(3, gender);
            stmt.setDate(4, sqlDob);
            stmt.setString(5, phone);
            stmt.setString(6, address);
            stmt.executeUpdate();
        }
    }

    private void logAdminAction(Connection conn, String adminUsername, String action, String description) throws SQLException {
        String insertLog = "INSERT INTO system_logs (admin_username, action, description, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(insertLog)) {
            stmt.setString(1, adminUsername);
            stmt.setString(2, action);
            stmt.setString(3, description);
            stmt.executeUpdate();
        }
    }

    // ========== UI Helper Methods ==========

    private JTextField createTextField(Color bg, Color fg) {
        JTextField field = new JTextField();
        field.setBackground(bg);
        field.setForeground(fg);
        field.setCaretColor(fg);
        field.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        return field;
    }

    private JLabel createLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        return label;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
    }
}
