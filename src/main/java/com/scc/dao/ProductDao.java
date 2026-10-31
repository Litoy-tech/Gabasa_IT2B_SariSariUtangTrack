package com.scc.dao;

import com.scc.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {
    public Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("p_name"),
                rs.getString("p_category"),
                rs.getDouble("p_price"),
                rs.getInt("stock_qty"),
                rs.getString("p_status")
        );
    }

    public boolean addProduct(String name, String category, double price, int stock){
        String sql = "INSERT INTO product_tbl(p_name, p_category, p_price, stock_qty) VALUES(?, ?, ?, ?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, stock);
            pstmt.executeUpdate();
            System.out.println("\n -> Product added successfully.");
            return true;
        }catch(SQLException e){
            System.err.println("Failed" + e.getMessage());
            return false;
        }
    }

    public boolean productExist(String name){
        String sql = "SELECT 1 FROM product_tbl WHERE LOWER(p_name) = LOWER(?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1,name);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        }catch (SQLException e){
            System.err.println("Database error: " + e.getMessage());
            return true;
        }
    }

    public List<Product> getProducts(){
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM product_tbl";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()){
                productList.add(mapProduct(rs));
            }
        }catch (SQLException e){
            System.err.println("Error Fetching all products: "+ e.getMessage());
        }
        return productList;
    }
}
