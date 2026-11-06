package com.scc.model;

public record SalesItem(int sales_items_id, int sales_id, int product_id, int quantity, double unit_price, double subtotal) {
    public double subtotal() {
        return unit_price * quantity;
    }
}
