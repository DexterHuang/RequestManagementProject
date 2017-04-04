package com.dexter.requestmanagement.Models;

public class User {

    private String email;

    private String firstName;

    public User() {

    }

    public User(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private String lastName;

    private String phoneNumber;


}
