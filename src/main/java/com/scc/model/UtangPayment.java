package com.scc.model;

public record UtangPayment(int payment_id, int customer_id, double amount, String payment_date, int received_by) {
}
