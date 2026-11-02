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

    public boolean updateProduct(int p_id, String name, String category, double price){
        String sql = "UPDATE product_tbl SET p_name = ?, p_category = ?, p_price = ? WHERE product_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, p_id);

            int rows = pstmt.executeUpdate();

            if(rows > 0){
                System.out.println("\n -> Product Updated.");
                return true;
            }else {
                System.out.println("\n -> Product ID not found.");
            }
        }catch (SQLException e){
            System.err.println("Error updating: " + e.getMessage());
        }
        return false;
    }

    public boolean restockProduct(int addedQty, int p_id){
        String sql = "UPDATE product_tbl SET stock_qty = stock_qty + ? WHERE product_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, addedQty);
            pstmt.setInt(2, p_id);

            int rows = pstmt.executeUpdate();

            if(rows > 0){
                System.out.println("\n -> Stock updated Successfully");
                return true;
            }else {
                System.out.println("\n -> Product ID not found.");
            }
        }catch (SQLException e){
            System.err.println("Error restocking: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int p_id, String status){
        String sql = "UPDATE product_tbl SET p_status = ? WHERE product_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, status);
            pstmt.setInt(2, p_id);

            int rows = pstmt.executeUpdate();

            if(rows > 0){
                System.out.println("\n Product ID: "+p_id+" Updated Successfully.");
                return true;
            }else {
                System.out.println("\n -> Product ID not found.");
            }
        }catch(SQLException e){
            System.err.println("Error : " + e.getMessage());
        }
        return false;
    }

    public boolean deleteProduct(int p_id){
        String sql = "DELETE FROM product_tbl WHERE product_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, p_id);

            int rowsDelete = pstmt.executeUpdate();

            if(rowsDelete > 0){
                System.out.println("\n Product ID: "+p_id+" Deleted Successfully.");
                return true;
            }else {
                System.out.println("\n -> Product ID not found.");
            }
        }catch (SQLException e){
            System.err.println("Error Deleting: " + e.getMessage());
        }
        return false;
    }
}
