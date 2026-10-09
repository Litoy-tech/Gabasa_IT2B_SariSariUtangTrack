package com.scc.model;

public record User(int user_id, String fullName, String username, String password, String roles, String status, String created_at) {
    @Override
    public String toString(){
        return String.format("[%d] %s %s (%s) - Status: %s", user_id, fullName, username, roles, status);

    }
}


