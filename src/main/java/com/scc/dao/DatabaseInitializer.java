package com.scc.dao;

import java.sql.*;

public class DatabaseInitializer {
    public static void initializer() {
        new UserDao();
        new ProductDao();
        new CustomerDao();
        new SalesDao();
        new AuditLogDao();
        System.out.println("\n -> Database tables verified.");
    }
}
