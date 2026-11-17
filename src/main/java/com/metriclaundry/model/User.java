package com.metriclaundry.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private boolean active = true;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** First name for friendly greetings, e.g. "Hi, Juan". */
    public String getFirstName() {
        if (fullName == null || fullName.isBlank()) return username;
        return fullName.trim().split("\\s+")[0];
    }

    @Override public String toString() { return fullName + " (" + username + ")"; }
}
