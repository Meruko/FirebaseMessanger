package com.example.firebasemessenger.models;

public class User {
    private String email;
    private String key;
    private String nickname;
    private String surname;
    private String name;

    public User() {
    }

    public User(String email, String key, String nickname, String surname, String name) {
        this.email = email;
        this.key = key;
        this.nickname = nickname;
        this.surname = surname;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
