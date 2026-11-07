package com.scc.model;

public record Audit(int log_id, Integer user_id, String username, String action, String table_affected, Integer record_id, String description, String log_stamp) {

    @Override
    public String toString(){
        return String.format("[%d] User: %s (ID: %s) | Action: %s | Table: %s | Record ID: %s | Description: %s | Timestamp: %s",
                log_id, username, user_id, action, table_affected, record_id, description, log_stamp);
    }
}
