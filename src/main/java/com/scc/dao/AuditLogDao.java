package com.scc.dao;

import com.scc.model.Audit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao {

    private Audit mapAudit(ResultSet rs) throws SQLException{
        int r_id = rs.getInt("record_id");
        Integer recordId = rs.wasNull() ? null : r_id;

        String user = rs.getString("username");
        if(user == null) user = "(unknown)";

        return new Audit(
                rs.getInt("log_id"),
                user,
                rs.getString("action"),
                rs.getString("table_affected"),
                recordId,
                rs.getString("Description"),
                rs.getString("log_timestamp")
        );
    }

    public List<Audit> getAllLogs(){
        List<Audit> audit_list = new ArrayList<>();

        String sql = "Select a.log_id, u.username, a.action, a.table_affected, a.record_id, a.description, a.log_timestamp " +
                "FROM audit_logs a LEFT JOIN users u On a.user_id = u.user_id ORDER BY a.log_timestamp DESC";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()){
                audit_list.add(mapAudit(rs));
            }
        }catch (SQLException e){
            System.err.println("Error fetching audit logs: " + e.getMessage());
        }
        return audit_list;
    }

    public List<Audit> getLogUserByID(int userID){
        List<Audit> audits_list = new ArrayList<>();

        String sql = "SELECT a.log_id, u.username, a.action, " +
                "a.table_affected, a.record_id, a.description, a.log_timestamp " +
                "FROM audit_logs a " +
                "LEFT JOIN users u ON a.user_id = u.user_id " +
                "WHERE a.user_id = ? " +
                "ORDER BY a.log_timestamp DESC";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement psmt = conn.prepareStatement(sql)){

            psmt.setInt(1, userID);

            try(ResultSet rs = psmt.executeQuery()){
                while(rs.next()){
                    audits_list.add(mapAudit(rs));
                }
            }
        }catch (SQLException e){
            System.err.println("Error fetching user audit logs: " + e.getMessage());
        }
        return audits_list;
    }

    public boolean logs(Integer userId, String action, String tableAffected, Integer recordId, String description){
        String sql = "INSERT INTO audit_logs(user_id, action, table_affected, record_id, description) VALUES(?, ?, ?, ?, ?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            if(userId == null){
                pstmt.setNull(1, Types.INTEGER);
            }else {
                pstmt.setInt(1, userId);
            }

            pstmt.setString(2, action);

            if(tableAffected == null){
                pstmt.setNull(3, Types.VARCHAR);
            }else {
                pstmt.setString(3, tableAffected);
            }

            if(recordId == null){
                pstmt.setNull(4, Types.INTEGER);
            }else {
                pstmt.setInt(4, recordId);
            }

            pstmt.setString(5, description);
            pstmt.executeUpdate();
            return true;
        }catch (SQLException e){
            System.err.println("Error saving audit log: " + e.getMessage());
            return false;
        }
    }

}
