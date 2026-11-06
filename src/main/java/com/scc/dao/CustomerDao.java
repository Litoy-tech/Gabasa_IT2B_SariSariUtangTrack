package com.scc.dao;

import com.scc.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("fullName"),
                rs.getString("contactNumber"),
                rs.getString("address"),
                rs.getString("status"),
                rs.getString("created_at")
        );
    }

    public int addCustomer(int userId, String fullName, String contact, String address,
                           double creditLimit, String agreement, String pinHash){
        String sql = "INSERT INTO customers(user_id, full_name, contact_number, address, credit_limit, agreement_on_file, pin_hash) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            pstmt.setInt(1, userId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, contact);
            pstmt.setString(4, address);
            pstmt.setDouble(5, creditLimit);
            pstmt.setString(6, agreement);
            pstmt.setString(7, pinHash);
            pstmt.executeUpdate();

            try(ResultSet keys = pstmt.getGeneratedKeys()){
                if(keys.next()){
                    System.out.println("\n -> Customer added successfully.");
                    return keys.getInt(1);
                }
            }
            return -1;
        }catch (SQLException e){
            System.err.println("Error adding customer: " + e.getMessage());
            return -1;
        }
    }

    public List<Customer> getAllCustomers(){
        List<Customer> customerList = new ArrayList<>();
        String sql = "SELECT * FROM customers";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()){
                customerList.add(mapCustomer(rs));
            }
        }catch (SQLException e){
            System.err.println("Error fetching customers: " + e.getMessage());
        }
        return customerList;
    }

    public boolean updateCustomer(int customerId, String fullName, String contact, String address,
                                  double creditLimit, String agreement){
        String sql = "UPDATE customers SET full_name = ?, contact_number = ?, address = ?, " +
                "credit_limit = ?, agreement_on_file = ? WHERE customer_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, fullName);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setDouble(4, creditLimit);
            pstmt.setString(5, agreement);
            pstmt.setInt(6, customerId);

            int rows = pstmt.executeUpdate();

            if(rows > 0){
                System.out.println("\n -> Customer Updated.");
                return true;
            }else {
                System.out.println("\n -> Customer ID not found.");
            }
        }catch (SQLException e){
            System.err.println("Error updating customer: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int customerId, String status){
        String sql = "UPDATE customers SET status = ? WHERE customer_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, status);
            pstmt.setInt(2, customerId);

            int rows = pstmt.executeUpdate();

            if(rows > 0){
                System.out.println("\n -> Customer status updated.");
                return true;
            }else {
                System.out.println("\n -> Customer ID not found.");
            }
        }catch (SQLException e){
            System.err.println("Error updating status: " + e.getMessage());
        }
        return false;
    }

    public boolean resetPin(int customerId, String newPinHash){
        String sql = "UPDATE customers SET pin_hash = ?, failed_pin_attempts = 0, " +
                "status = CASE WHEN status = 'LOCKED' THEN 'ACTIVE' ELSE status END " +
                "WHERE customer_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, newPinHash);
            pstmt.setInt(2, customerId);

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
}
