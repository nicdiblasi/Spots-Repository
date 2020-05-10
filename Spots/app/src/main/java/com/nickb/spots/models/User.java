package com.nickb.spots.models;

public class User {


    private String email;
    private String username;
    private String user_id;

    public User(String email, String username, String user_id) {
        this.email = email;
        this.username = username;
        this.user_id = user_id;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
