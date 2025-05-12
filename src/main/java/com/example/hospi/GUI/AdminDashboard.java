package com.example.hospi.GUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AdminDashboard {
    private int adminId;
    private String adminUsername = "";
    private String adminEmail = "";

    private JLabel adminUsernameLabel;
    private JLabel adminEmailLabel;

    public AdminDashboard(int adminId) throws SQLException {
        this.adminId = adminId;
        loadAdminDetails(); // now loads both username and email
        showDashboard();
    }

    private void loadAdminDetails() {
        String query = "SELECT u.username, a.email FROM admins a JOIN users u ON a.user_id = u.id WHERE a.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                adminUsername = rs.getString("username");
                adminEmail = rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showDashboard() {
        JFrame frame = new JFrame("Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 750);
        frame.setLocationRelativeTo(null);

        UserService userService = new UserService();
        SystemLogService logService = new SystemLogService();

        // Colors
        Color bgBase = new Color(26, 32, 44);
        Color accent = new Color(0, 188, 212);
        Color panelGray = new Color(37, 47, 63);
        Color textLight = new Color(230, 230, 230);
        Color textSubtle = new Color(180, 180, 180);

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(bgBase);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel profilePanel = new JPanel(new GridLayout(2, 1));
        profilePanel.setOpaque(false);
        adminUsernameLabel = new JLabel("👤 Username: " + adminUsername);
        adminUsernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        adminUsernameLabel.setForeground(textLight);
        adminEmailLabel = new JLabel("📧 " + adminEmail);
        adminEmailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        adminEmailLabel.setForeground(textSubtle);
        profilePanel.add(adminUsernameLabel);
        profilePanel.add(adminEmailLabel);

        JLabel titleLabel = new JLabel("🛠️ Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(accent);

        headerPanel.add(profilePanel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Username", "Role"};
        DefaultTableModel userModel = new DefaultTableModel(columns, 0);
        JTable userTable = new JTable(userModel);
        userService.loadUsers(userModel);

        userTable.setRowHeight(28);
        userTable.setShowGrid(true);
        userTable.setGridColor(accent);
        userTable.setBackground(panelGray);
        userTable.setForeground(Color.WHITE);
        userTable.setSelectionBackground(accent);

        JTableHeader tableHeader = userTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(accent);
        tableHeader.setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(accent),
                "👥 User Accounts",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                accent
        ));
        scrollPane.getViewport().setBackground(bgBase);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Control buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton viewLogsBtn = new JButton("View Logs");
        JButton generateReportBtn = new JButton("Generate Report");
        JButton addUserBtn = new JButton("Add User");
        JButton deleteUserBtn = new JButton("Delete User");

        styleButton(viewLogsBtn, "📁", accent);
        styleButton(generateReportBtn, "📊", accent);
        styleButton(addUserBtn, "➕", accent);
        styleButton(deleteUserBtn, "🗑️", accent);

        controlPanel.add(viewLogsBtn);
        controlPanel.add(generateReportBtn);
        controlPanel.add(addUserBtn);
        controlPanel.add(deleteUserBtn);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // Add User Dialog
        addUserBtn.addActionListener(e -> new AddUserDialog(frame, userService, userModel, adminId));

        // Delete User logic
// Delete User logic
        deleteUserBtn.addActionListener(e -> {
            int selected = userTable.getSelectedRow();
            if (selected != -1) {
                int userId = (int) userModel.getValueAt(selected, 0);
                String role = userModel.getValueAt(selected, 2).toString();
                String username = userModel.getValueAt(selected, 1).toString();

                if (role.equalsIgnoreCase("admin")) {
                    JOptionPane.showMessageDialog(frame, "Cannot delete an admin account.");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this user?");
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userService.deleteUser(userId)) {
                        // Log the action
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement stmt = conn.prepareStatement(
                                     "INSERT INTO system_logs (admin_username, action, description, timestamp) VALUES (?, ?, ?, NOW())"
                             )) {
                            stmt.setString(1, adminUsername); // logged-in admin
                            stmt.setString(2, "DELETE_USER");
                            stmt.setString(3, "Deleted user: " + username + " (" + role + ")");
                            stmt.executeUpdate();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                        JOptionPane.showMessageDialog(frame, "User deleted successfully.");
                        userService.loadUsers(userModel);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to delete user.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a user to delete.");
            }
        });


        // View Logs & Generate Report (use same log view)
        ActionListener showLogsAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] logColumns = {"Admin", "Action", "Description", "Timestamp"};
                DefaultTableModel logsModel = new DefaultTableModel(logColumns, 0);

                logService.getLogsByAdmin(adminUsername, logsModel);

                JTable logsTable = new JTable(logsModel);
                logsTable.setRowHeight(25);
                JScrollPane logScroll = new JScrollPane(logsTable);
                logScroll.setPreferredSize(new Dimension(800, 400));
                JOptionPane.showMessageDialog(frame, logScroll, "📋 System Logs", JOptionPane.PLAIN_MESSAGE);
            }
        };

        viewLogsBtn.addActionListener(showLogsAction);
        generateReportBtn.addActionListener(showLogsAction);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private void styleButton(JButton button, String emojiPrefix, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setText(emojiPrefix + " " + button.getText());
    }
}
