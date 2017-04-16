package com.dexter.requestmanagement.Models;

public class User {

    private String email;

    private String firstName;

    private UserRoleType role;

    public User() {

    }

    public User(String email, String firstName, String lastName, UserRoleType role) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
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


    public void setRole(UserRoleType role) {
        this.role = role;
    }

    public UserRoleType getRole() {

        return role;
    }
}
