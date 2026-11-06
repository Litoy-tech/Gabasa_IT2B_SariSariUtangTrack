package com.scc.model;

public record Customer(int customer_id, String fullName, String contactNumber, String address, String status, String created_at) {
    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - Status: %s", customer_id, fullName, contactNumber, status);
    }
}
