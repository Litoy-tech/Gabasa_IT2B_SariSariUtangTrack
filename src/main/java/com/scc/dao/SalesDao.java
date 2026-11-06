package com.scc.dao;

import com.scc.model.Sales;

import java.sql.ResultSet;
import java.sql.*;

public class SalesDao {

    private static class SalesExeption extends Exception {
        SalesExeption(String message){
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

}
