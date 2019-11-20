package com.example.fyp_v2.Class;

public class Customer {

    String customerID;
    String customerName;
    String password;
    String email;

    public Customer(String customerID, String customerName, String password, String email) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.password = password;
        this.email = email;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}