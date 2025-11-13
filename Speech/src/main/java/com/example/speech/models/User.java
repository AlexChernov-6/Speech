package com.example.speech.models;

import java.time.LocalDate;

public class User {

    private int id;
    private String email;
    private String visibleName;
    private String userName;
    private String password;
    private LocalDate birthday;

    public User() {}

    public User(String email, String visibleName, String userName, String password, LocalDate birthday) {
        this.email = email;
        this.visibleName = visibleName;
        this.userName = userName;
        this.password = password;
        this.birthday = birthday;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVisibleName() {
        return visibleName;
    }

    public void setVisibleName(String visibleName) {
        this.visibleName = visibleName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}
