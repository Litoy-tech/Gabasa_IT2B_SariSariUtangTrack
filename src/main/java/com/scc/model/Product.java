package com.scc.model;

public record Product(int product_id, String p_name, String p_category, double p_price, int stock_qty, String p_status) {
    @Override
    public String toString(){
        return String.format("[%d] %s | %s | ₱%.2f | Stock: %d | Status: %s", product_id, p_name, p_category, p_price, stock_qty, p_status);
    }

}
