package com.scc.model;

public record Sales(int sales_id, int cashier_id, int customer_id, String payment_type, String Sales_date, double total_amount, double amount_received,
                    double change_amount) {

    @Override
    public String toString(){
        String customer = (customer_id == 0) ? "-" : String.valueOf(customer_id);
        return String.format("[%d] %s | %s | Customer: %s | Total: ₱%.2f",
                sales_id, Sales_date, payment_type, customer, total_amount);
    }
}
