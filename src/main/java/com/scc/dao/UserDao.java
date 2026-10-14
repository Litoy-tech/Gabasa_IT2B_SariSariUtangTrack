package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class UserDao {

    public boolean registerUser(String fullName ,String username, String password, String role){

        String sql = "INSERT INTO users(fullName, username, password, roles, status) VALUES(?, ?, ?, ?, 'PENDING')";

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, fullName);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("\n-> Successful Registration, Waiting for Approval");
            return true;

        } catch (SQLException e) {
            System.err.println("Failed" + e.getMessage());
            return false;
        }

    }

    public boolean usernameExists(String username){
        String sql = "SELECT 2 FROM users WHERE username = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        } catch (SQLException e){
            System.out.println("Database error: " + e.getMessage());
            return true;
        }
    }

    public User login(String username, String password){
        String sql = "Select * FROM users WHERE username = ? AND password = ?";

        try(Connection conn = DatabaseConnection.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ps.setString(2, password);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    String status = rs.getString("status");
                    if("PENDING".equalsIgnoreCase(status)){
                        System.out.println(" -> Account is pending, Waiting for Approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("fullName"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    );
                }else{
                    System.out.println(" -> Invalid username or password");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null;
    }
}
