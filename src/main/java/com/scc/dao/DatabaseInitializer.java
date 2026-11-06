package com.scc.dao;

public class DatabaseInitializer {
    public static void initializer() {
        new UserDao();
        new ProductDao();
        new CustomerDao();
        new AuditLogDao();
        System.out.println("\n -> Database tables Verified. ");
    }
}
