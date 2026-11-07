package com.scc.dao;

import com.scc.model.Sales;
import com.scc.model.SalesItem;

import java.sql.ResultSet;
import java.sql.*;
import java.util.List;

public class SalesDao {

    private static class SalesException extends Exception {
        SalesException(String message){
            super(message);
        }
    }

    private double round2(double v){
        return Math.round(v * 100.0) / 100.0;
    }

    private Sales mapSales(ResultSet rs) throws SQLException {
        return new Sales(
                rs.getInt("sale_id"),
                rs.getInt("cashier_id"),
                rs.getInt("customer_id"),     // 0 kung Cash (NULL)
                rs.getString("payment_type"),
                rs.getString("sale_date"),
                rs.getDouble("total_amount"),
                rs.getDouble("amount_received"),
                rs.getDouble("change_amount"));
    }

    private int lastInsertId(Connection conn) throws SQLException {
        try(Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")){
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public int processSale(int cashier_id, Integer customerId, String typePayment, List<SalesItem> itemList, double amountReceived){
        String insertSale  = "INSERT INTO sales(cashier_id, customer_id, payment_type, total_amount, amount_received, change_amount) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        String insertItem = "INSERT INTO sale_items(sale_id, product_id, quantity, unit_price, subtotal) VALUES(?, ?, ?, ?, ?)";
        String getProduct = "SELECT p_name, p_price, stock_qty, p_status FROM product_tbl WHERE product_id = ?";
        String deduct = "UPDATE product_tbl SET stock_qty = stock_qty - ? WHERE product_id = ? AND stock_qty >= ?";
        String getCust = "SELECT credit_limit, balance, status FROM customer_tbl WHERE customer_id = ?";
        String addUtang = "UPDATE customer_tbl SET balance = balance + ?, " +
                "due_date = CASE WHEN ROUND(balance, 2) = 0 THEN date('now', 'localtime', '+30 days') ELSE due_date END WHERE customer_id = ?";

        boolean isUtang = "Utang".equalsIgnoreCase(typePayment);

        try(Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);

            try{
                if(itemList == null || itemList.isEmpty()){
                    throw new SalesException("Cart is Empty.");
                }

                double total = 0;
                double[] price = new double[itemList.size()];

                try(PreparedStatement pstmt = conn.prepareStatement(getProduct)){
                    for(int i = 0; i < itemList.size(); i++){
                        SalesItem it = itemList.get(i);
                        pstmt.setInt(1, it.product_id());
                        try(ResultSet rs = pstmt.executeQuery()){
                            if(!rs.next()){
                                throw new SalesException("Product ID " + it.product_id() + " not found.");
                            }
                            String name = rs.getString("p_name");
                            if(!"AVAILABLE".equalsIgnoreCase(rs.getString("p_status"))){
                                throw new SalesException(name + " is UNAVAILABLE.");
                            }
                            if(rs.getInt("stock_qty") < it.quantity()){
                                throw new SalesException("Not enough stock for " + name +
                                        " (stock: " + rs.getInt("stock_qty") + ").");
                            }
                            price[i] = rs.getDouble("p_price");
                            total += round2(price[i] * it.quantity());
                        }
                    }
                }

                total = round2(total);

                double received;
                double change;

                if(isUtang){
                    if(customerId == null){
                        throw new SalesException("Utang sale needs a customer.");
                    }
                    try(PreparedStatement pstmt = conn.prepareStatement(getCust)){
                        pstmt.setInt(1, customerId);
                        try(ResultSet rs = pstmt.executeQuery()){
                            if(!rs.next()){
                                throw new SalesException("Customer ID not found.");
                            }
                            if(!"ACTIVE".equalsIgnoreCase(rs.getString("status"))){
                                throw new SalesException("Customer is not ACTIVE.");
                            }
                            double available = rs.getDouble("credit_limit") - rs.getDouble("balance");
                            if(total > available){
                                throw new SalesException(String.format("Credit limit exceeded. Available credit: ₱%.2f", available));
                            }
                        }
                    }
                    received = 0;
                    change = 0;
                }else {
                    if(amountReceived < total){
                        throw new SalesException("Amount received is less than the total.");
                    }
                    received = amountReceived;
                    change = round2(amountReceived - total);
                }

                int saleId;
                try(PreparedStatement pstmt = conn.prepareStatement(insertSale)){
                    pstmt.setInt(1, cashier_id);
                    if(isUtang){
                        pstmt.setInt(2, customerId);
                    }else {
                        pstmt.setNull(2, Types.INTEGER);
                    }
                    pstmt.setString(3, isUtang ? "Utang" : "Cash");
                    pstmt.setDouble(4, total);
                    pstmt.setDouble(5, received);
                    pstmt.setDouble(6, change);
                    pstmt.executeUpdate();
                    saleId = lastInsertId(conn);
                }

                try(PreparedStatement psItem = conn.prepareStatement(insertItem);
                    PreparedStatement psStock = conn.prepareStatement(deduct)){

                    for(int i = 0; i < itemList.size(); i++){
                        SalesItem it = itemList.get(i);
                        double subtotal = round2(price[i] * it.quantity());

                        psItem.setInt(1, saleId);
                        psItem.setInt(2, it.product_id());
                        psItem.setInt(3, it.quantity());
                        psItem.setDouble(4, price[i]);
                        psItem.setDouble(5, subtotal);
                        psItem.executeUpdate();

                        psStock.setInt(1, it.quantity());
                        psStock.setInt(2, it.product_id());
                        psStock.setInt(3, it.quantity());
                        if(psStock.executeUpdate() == 0){
                            throw new SalesException("Stock changed while processing. Try again.");
                        }
                    }
                }

                if(isUtang){
                    try(PreparedStatement ps = conn.prepareStatement(addUtang)){
                        ps.setDouble(1, total);
                        ps.setInt(2, customerId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return saleId;

            }catch (SalesException e){
                conn.rollback();
                System.out.println("\n -> [ERROR] " + e.getMessage());
            }catch (SQLException e){
                conn.rollback();
                System.err.println("Error processing sale: " + e.getMessage());
            }
        }catch (SQLException e){
            System.err.println("Database error: " + e.getMessage());
        }
        return -1;
    }

    public int recordUtangPayment(int customerId, double amount, int receivedBy) {
        String getCust = "SELECT balance FROM customer_tbl WHERE customer_id = ?";
        String insertPay = "INSERT INTO utang_payments(customer_id, amount, received_by) VALUES(?, ?, ?)";
        String deductBal = "UPDATE customer_tbl SET balance = balance - ?, " +
                "due_date = CASE WHEN ROUND(balance - ?, 2) <= 0 THEN NULL ELSE due_date END " +
                "WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (amount <= 0) {
                    throw new SalesException("Invalid payment amount.");
                }

                try (PreparedStatement ps = conn.prepareStatement(getCust)) {
                    ps.setInt(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SalesException("Customer ID not found.");
                        }
                        double balance = round2(rs.getDouble("balance"));
                        if (balance <= 0) {
                            throw new SalesException("Customer has no utang balance.");
                        }
                        if (round2(amount) > balance) {
                            throw new SalesException(String.format("Payment exceeds balance (₱%.2f).", balance));
                        }
                    }
                }

                int paymentId;
                try (PreparedStatement ps = conn.prepareStatement(insertPay)) {
                    ps.setInt(1, customerId);
                    ps.setDouble(2, round2(amount));
                    ps.setInt(3, receivedBy);
                    ps.executeUpdate();
                    paymentId = lastInsertId(conn);
                }

                try (PreparedStatement ps = conn.prepareStatement(deductBal)) {
                    ps.setDouble(1, round2(amount));
                    ps.setDouble(2, round2(amount));
                    ps.setInt(3, customerId);
                    ps.executeUpdate();
                }

                conn.commit();
                return paymentId;

            } catch (SalesException e) {
                conn.rollback();
                System.out.println("\n -> [ERROR] " + e.getMessage());
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error recording payment: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return -1;
    }
}
