package com.scc.dao;

import java.sql.*;

public class DatabaseInitializer {

    private static void createTables() {
        String[] tables = {
                // 1. users
                "CREATE TABLE IF NOT EXISTS users (" +
                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT, fullName TEXT NOT NULL, " +
                        "username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, roles TEXT NOT NULL, " +
                        "status TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')))",

                // 2. product_tbl
                "CREATE TABLE IF NOT EXISTS product_tbl (" +
                        "product_id INTEGER PRIMARY KEY AUTOINCREMENT, p_name TEXT UNIQUE NOT NULL, " +
                        "p_category TEXT NOT NULL, p_price REAL NOT NULL CHECK (p_price > 0), " +
                        "stock_qty INTEGER NOT NULL DEFAULT 0 CHECK (stock_qty >= 0), " +
                        "p_status TEXT NOT NULL DEFAULT 'AVAILABLE' CHECK (p_status IN ('AVAILABLE', 'UNAVAILABLE')))",

                // 3. customer_tbl
                "CREATE TABLE IF NOT EXISTS customer_tbl (" +
                        "customer_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER REFERENCES users (user_id), " +
                        "full_name TEXT NOT NULL, contact_number TEXT NOT NULL, address TEXT NOT NULL, " +
                        "credit_limit REAL NOT NULL DEFAULT 0 CHECK (credit_limit >= 0), " +
                        "balance REAL NOT NULL DEFAULT 0 CHECK (balance >= 0), due_date TEXT, " +
                        "agreement_on_file TEXT NOT NULL DEFAULT 'No' CHECK (agreement_on_file IN ('Yes', 'No')), " +
                        "pin_hash TEXT NOT NULL, failed_pin_attempts INTEGER NOT NULL DEFAULT 0 CHECK (failed_pin_attempts >= 0), " +
                        "status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')))",

                // 4. sales
                "CREATE TABLE IF NOT EXISTS sales (" +
                        "sale_id INTEGER PRIMARY KEY AUTOINCREMENT, cashier_id INTEGER NOT NULL, customer_id INTEGER, " +
                        "payment_type TEXT NOT NULL CHECK (payment_type IN ('Cash', 'Utang')), " +
                        "sale_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
                        "total_amount REAL NOT NULL CHECK (total_amount >= 0), amount_received REAL NOT NULL DEFAULT 0, " +
                        "change_amount REAL NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY (cashier_id) REFERENCES users(user_id), " +
                        "FOREIGN KEY (customer_id) REFERENCES customer_tbl(customer_id))",

                // 5. sale_items
                "CREATE TABLE IF NOT EXISTS sale_items (" +
                        "sale_item_id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id INTEGER NOT NULL, product_id INTEGER NOT NULL, " +
                        "quantity INTEGER NOT NULL CHECK (quantity > 0), unit_price REAL NOT NULL CHECK (unit_price >= 0), " +
                        "subtotal REAL NOT NULL CHECK (subtotal >= 0), " +
                        "FOREIGN KEY (sale_id) REFERENCES sales(sale_id), " +
                        "FOREIGN KEY (product_id) REFERENCES product_tbl(product_id))",

                // 6. utang_payments
                "CREATE TABLE IF NOT EXISTS utang_payments (" +
                        "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, customer_id INTEGER NOT NULL, " +
                        "amount REAL NOT NULL CHECK (amount > 0), " +
                        "payment_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), received_by INTEGER NOT NULL, " +
                        "FOREIGN KEY (customer_id) REFERENCES customer_tbl(customer_id), " +
                        "FOREIGN KEY (received_by) REFERENCES users(user_id))",

                // 7. audit_logs
                "CREATE TABLE IF NOT EXISTS audit_logs (" +
                        "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE, " +
                        "action TEXT NOT NULL, table_affected TEXT, record_id INTEGER, " +
                        "description TEXT NOT NULL, " +
                        "log_timestamp TEXT NOT NULL DEFAULT (datetime('now', 'localtime')))"
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public static void initializer() {
        createTables();          // tables first, before any DAO uses them
        new UserDao();
        new ProductDao();
        new CustomerDao();
        new SalesDao();
        new AuditLogDao();
        System.out.println("\n -> Database tables verified.");
    }
}