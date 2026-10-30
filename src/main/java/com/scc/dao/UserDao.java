package com.scc.dao;

import com.scc.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    public UserDao() {

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            defaultAdminGenerator(conn);
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }

    }

    private User mapUser(ResultSet rs) throws SQLException{
        return new User(
                rs.getInt("user_id"),
                rs.getString("fullName"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("roles"),
                rs.getString("status"),
                rs.getString("created_at")
        );
    }

    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE roles ='ADMIN'";
        String insertSql = "INSERT INTO users(fullName, username, password, roles, status) VALUES('Admin', 'admin@gmail.com', 'admin123', 'ADMIN', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("-> Default Admin created");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }

    }


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
                    if("INACTIVE".equalsIgnoreCase(status)){
                        System.out.println(" -> Account is deactivated. Contact the admin.");
                        return null;
                    }
                    return mapUser(rs);
                }else{
                    System.out.println(" -> Invalid username or password");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUser(){
        List<User> usersList = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()){
                usersList.add(mapUser(rs));
            }
        }catch (SQLException e){
            System.err.println("Error Fetching all users: "+ e.getMessage());
        }
        return usersList;
    }

    public List<User> getPendingUser(){
        List<User> pendingList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'PENDING'";

        try(Connection conn = DatabaseConnection.getConnection()){
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                pendingList.add(mapUser(rs));
            }
        }catch (SQLException e){
            System.err.println("Error fetching users" + e.getMessage());
        }
        return pendingList;
    }

    public boolean approvedUser(int userId){
        String sql = "UPDATE users SET status = 'APPROVED' WHERE user_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement psmt = conn.prepareStatement(sql)){
            psmt.setInt(1, userId);
            int rowsUpdated = psmt.executeUpdate();

            if(rowsUpdated > 0){
                System.out.println("\n -> User ID: "+userId+" Approved Successfully" );
                return true;
            }else {
                System.out.println("\n -> User ID not found!");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching users" + e.getMessage());
        }
        return false;
    }

    public boolean deletedUser(int userId){
        String sql = "DELETE FROM users WHERE user_id = ?";

        try(Connection  conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsDelete = pstmt.executeUpdate();

            if (rowsDelete > 0) {
                System.out.println("\n -> User ID: " + userId + " has been deleted");
                return true;
            } else {
                System.out.println(" -> User ID not found!");
            }
        }catch (SQLIntegrityConstraintViolationException e){
            System.out.println(" -> [ERROR] Cannot delete: this user has existing records. Set the status to INACTIVE instead.");
        }catch (SQLException e){
            System.err.println("Error fetching users" + e.getMessage());
        }
        return false;
    }

    public boolean addUser(String fullname, String username, String password, String roles){
        String sql = "INSERT INTO users(fullName, username, password, roles, status) VALUES(?, ?, ?, ?, 'PENDING')";

        if(usernameExists(username)){
            System.out.println("Username already exists!");
            return false;
        }

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, fullname);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, roles);
            pstmt.executeUpdate();
            System.out.println("\n-> Successful Registration, Waiting for Approval");
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding users" + e.getMessage());
            return false;
        }
    }

    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus){
           String sql = "UPDATE users SET roles = ?, status = ? WHERE user_id = ?";

           try(Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)){

               pstmt.setString(1, newRole.toUpperCase());
               pstmt.setString(2, newStatus.toUpperCase());
               pstmt.setInt(3, userId);

               int rowUpdate = pstmt.executeUpdate();

               if(rowUpdate > 0){
                   System.out.println("\n -> User ID: "+userId+" Updated Successfully.");
                   return true;
               }else{
                   System.out.println("\n -> User ID not found.");
               }
               return false;
           }catch (SQLException e){
               System.err.println("Error updating user: " + e.getMessage());
           }
        return false;
    }

}
