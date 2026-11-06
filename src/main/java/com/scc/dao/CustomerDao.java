package com.scc.dao;


import com.scc.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    private Customer mapCustomer (ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("contact_number"),
                rs.getString("address"),
                rs.getDouble("credit_limit"),
                rs.getDouble("balance"),
                rs.getString("due_date"),
                rs.getString("agreement_on_file"),
                rs.getString("status"));
    }

    public int registerCustomer(String fullName,String username, String password, String contact, String address,
                                double creditLimit, String agreement, String pinHash){
        String userSql = "INSERT INTO users(fullName, username, password, roles, status) VALUES(?, ?, ?, 'CUSTOMER', 'APPROVED')";
        String cusSql = "INSERT INTO customer_tbl(user_id, full_name, contact_number, address, credit_limit, agreement_on_file, pin_hash)" +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try(Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);

            try(PreparedStatement us = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement cs = conn.prepareStatement(cusSql, Statement.RETURN_GENERATED_KEYS)){

                us.setString(1, fullName);
                us.setString(2, username);
                us.setString(3, password);
                us.executeUpdate();

                int userId;
                try(ResultSet keys = us.getGeneratedKeys()){
                    if(!keys.next()){
                        conn.rollback();
                        return -1;
                    }
                    userId = keys.getInt(1);
                }

                cs.setInt(1, userId);
                cs.setString(2, fullName);
                cs.setString(3, contact);
                cs.setString(4, address);
                cs.setDouble(5, creditLimit);
                cs.setString(6, agreement);
                cs.setString(7, pinHash);
                cs.executeUpdate();

                try(ResultSet keys = cs.getGeneratedKeys()){
                    if(keys.next()){
                        int customerId = keys.getInt(1);
                        conn.commit();
                        System.out.println("\n -> Customer Successfully added");
                        return customerId;
                    }
                }
                conn.rollback();
                return -1;
            }
        }catch (SQLException e){
            System.err.println("Error adding customer: " + e.getMessage());
            return -1;
        }
    }

    public List<Customer> getAllCustomer(){
        List<Customer> customerList = new ArrayList<>();
        String sql = "SELECT * FROM customer_tbl";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()){
                customerList.add(mapCustomer(rs));
            }
        }catch (SQLException e){
            System.err.println("Error Fetching all customers: "+ e.getMessage());
        }
        return customerList;
    }

    public boolean updateCustomers(int customerId, String fullName, String contact, String address, double creditLimit, String agreement){
        String sql = "UPDATE customer_tbl SET full_name = ?, contact_number = ?, address = ?," +
                "credit_limit = ?, agreement_on_file = ? WHERE customer_id = ?";
        String userSql = "UPDATE users SET fullName = ? WHERE user_id = (SELECT user_id FROM customer_tbl WHERE customer_id = ?)";

        try(Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);

            try(PreparedStatement pstmt = conn.prepareStatement(sql);
                PreparedStatement us = conn.prepareStatement(userSql)){

                pstmt.setString(1, fullName);
                pstmt.setString(2, contact);
                pstmt.setString(3, address);
                pstmt.setDouble(4, creditLimit);
                pstmt.setString(5, agreement);
                pstmt.setInt(6, customerId);

                int rows = pstmt.executeUpdate();

                if(rows == 0){
                    conn.rollback();
                    System.out.println("\n -> Customer ID not found.");
                    return false;
                }

                us.setString(1, fullName);
                us.setInt(2, customerId);
                us.executeUpdate();

                conn.commit();
                System.out.println("\n -> Customer Updated.");
                return true;
            }catch (SQLException e){
                conn.rollback();
                throw e;
            }

        }catch (SQLException e){
            System.err.println("Error updating customer: " + e.getMessage());
        }
        return false;
    }

    public boolean updatePin(int customerID, String pin){
        String sql = "UPDATE customer_tbl SET pin_hash = ?, failed_pin_attempts = 0, status = CASE WHEN status = 'LOCKED' " +
                " THEN 'ACTIVE' ELSE status END WHERE customer_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, pin);
            pstmt.setInt(2, customerID);


            int rows = pstmt.executeUpdate();

            if(rows > 0){
                System.out.println("\n -> PIN reset successfully.");
                return true;
            }else {
                System.out.println("\n -> Customer ID not found.");
            }
        }catch (SQLException e){
            System.err.println("Error resetting PIN: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int customerId, String status){
        String sql = "UPDATE customer_tbl SET status = ? WHERE customer_id = ?";
        String userSql = "UPDATE users SET status = ? WHERE user_id = (SELECT user_id FROM customer_tbl WHERE customer_id = ?)";
        String userStatus = status.equals("INACTIVE") ? "INACTIVE" : "APPROVED";

        try(Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);

            try(PreparedStatement pstmt = conn.prepareStatement(sql);
                PreparedStatement us = conn.prepareStatement(userSql)){

                pstmt.setString(1, status);
                pstmt.setInt(2, customerId);
                int rows = pstmt.executeUpdate();

                if(rows == 0){
                    conn.rollback();
                    System.out.println("\n -> Customer ID not found.");
                    return false;
                }

                us.setString(1, userStatus);
                us.setInt(2, customerId);
                us.executeUpdate();

                conn.commit();
                System.out.println("\n -> Customer status updated.");
                return true;
            }catch (SQLException e){
                conn.rollback();
                throw e;
            }
        }catch (SQLException e){
            System.err.println("Error updating status: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteCustomer(int customerId){
        try(Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);

            try{
                int userId = 0;
                try(PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM customer_tbl WHERE customer_id = ?")){
                    ps.setInt(1, customerId);
                    try(ResultSet rs = ps.executeQuery()){
                        if(!rs.next()){
                            conn.rollback();
                            System.out.println("\n -> Customer ID not found.");
                            return false;
                        }
                        userId = rs.getInt("user_id");
                    }
                }

                try(PreparedStatement ps = conn.prepareStatement("DELETE FROM customer_tbl WHERE customer_id = ?")){
                    ps.setInt(1, customerId);
                    ps.executeUpdate();
                }

                if(userId > 0){
                    try(PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")){
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                System.out.println("\n -> Customer ID: " + customerId + " Deleted Successfully.");
                return true;
            }catch (SQLException e){
                conn.rollback();
                throw e;
            }
        }catch (SQLException e){
            if(e.getMessage() != null && e.getMessage().toUpperCase().contains("FOREIGN KEY")){
                System.out.println("\n -> [ERROR] Cannot delete: this customer has existing records. Set the status to INACTIVE instead.");
            }else {
                System.err.println("Error deleting customer: " + e.getMessage());
            }
        }
        return false;
    }
}