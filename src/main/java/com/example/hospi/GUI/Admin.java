package com.example.hospi.GUI;


public class Admin extends User {
    private int id;
    private int userId;
    private String name;
    private String email;
    private String phone;

    public Admin(int id, int userId, String name, String email, String phone) {
        super(userId);
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}

