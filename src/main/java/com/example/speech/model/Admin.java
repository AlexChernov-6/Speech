package com.example.speech.model;

import jakarta.persistence.*;

import java.nio.charset.StandardCharsets;

@Entity
@Table(name = "admins", schema = "admins_schema")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private short adminID;
    @Column(name = "admin_login")
    private byte[] adminLogin;
    @Column(name = "admin_password")
    private byte[] adminPassword;

    public short getAdminID() {
        return adminID;
    }

    public void setAdminID(short adminID) {
        this.adminID = adminID;
    }

    public String getAdminLogin() {
        return new String(adminLogin, StandardCharsets.UTF_8);
    }

    public void setAdminLogin(String adminLogin) {
        this.adminLogin = adminLogin.getBytes();
    }

    public String getAdminPassword() {
        return new String(adminPassword, StandardCharsets.UTF_8);
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword.getBytes();
    }
}
