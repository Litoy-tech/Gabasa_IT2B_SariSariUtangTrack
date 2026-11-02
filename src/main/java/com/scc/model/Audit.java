package com.scc.model;

public record Audit(int log_id, String username, String action, String table_affected, int record_id, String description, String log_stamp) {

    @Override
    public String toString(){
        return String.format("[%d] %d | %d | %s | %s | %d | %s | %s |", log_id, username, table_affected, record_id, description, log_stamp);
    }
}
