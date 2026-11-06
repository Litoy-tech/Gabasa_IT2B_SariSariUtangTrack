package com.scc.model;

public record Customer(int customer_id, int user_id, String full_name, String contact_number, String address,
                       double credit_limit, double balance, String due_date, String agreement_on_file, String status){
    @Override
    public String toString(){
        return String.format("[%d] %s (%s) | Limit: %.2f | Balance: %.2f | Status: %s",
                customer_id, full_name, contact_number, credit_limit, balance, status);
    }
}
