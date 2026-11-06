package com.scc.model;

public record Audit(int log_id, String username, String action, String table_affected, Integer record_id, String description, String log_stamp) {

    @Override
    public String toString(){
        return String.format("[%d] User: %s | Action: %s | Table: %s | Record ID: %d | Description: %s | Timestamp: %s",
                log_id, username, action, table_affected, record_id, description, log_stamp);
    }
}
