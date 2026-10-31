package com.scc.dao;

public class DatabaseInitializer {
    public static void initializer() {
        new UserDao();
        System.out.println("\n -> Database tables Verified. ");
    }
}
