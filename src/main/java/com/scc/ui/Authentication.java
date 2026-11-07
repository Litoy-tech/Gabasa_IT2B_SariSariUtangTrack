package com.scc.ui;

import com.scc.dao.*;
import com.scc.model.Customer;
import com.scc.model.User;
import com.scc.ui.admin.AdminMenu;
import com.scc.ui.cashier.CashierMenu;
import com.scc.ui.systemAuditor.SystemAuditorMenu;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ProductDao productDao;
    private final CustomerDao customerDao;
    private final SalesDao salesDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

    public Authentication(UserDao userdao,CustomerDao customerDao, ProductDao productDao, SalesDao salesDao,AuditLogDao logDao,Scanner sc){
        this.userDao = userdao;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.salesDao = salesDao;
        this.logDao = logDao;
        this.sc = sc;
    }

    public void start(){
        while(true) {

            System.out.println("\n========================================");
            System.out.println("             SARI SARI STORE");
            System.out.println("========================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    userLogin();
                    break;
                case "2":
                    registerUser();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid Choice, Try Again.");
            }
        }
    }

    private void userLogin(){
        System.out.println("\n    ===== LOG IN =====");

        String user;
        while (true) {
            System.out.print("Enter Username: ");
            user = sc.nextLine().trim();

            if(user.isEmpty()){
                System.out.println("Invalid! username cannot be empty.");
            }else {
                break;
            }
        }

        String pass;
        while (true) {
            System.out.print("Enter Password: ");
            pass = sc.nextLine().trim();

            if(pass.isEmpty()){
                System.out.println("Invalid! password cannot be empty.");
            }else{
                break;
            }
        }

        User loggedIn = userDao.login(user, pass);
        if(loggedIn != null){
            Menu menu;
            switch (loggedIn.roles()){
                case "ADMIN":
                    menu = new AdminMenu( userDao, customerDao, productDao, salesDao,logDao, sc);
                    break;
                case "CASHIER":
                    menu = new CashierMenu(userDao, productDao, customerDao, salesDao,logDao,sc);
                    break;
                case "SYS_AUDITOR":
                    menu = new SystemAuditorMenu(userDao, customerDao, productDao,salesDao,logDao,sc);
                    break;
                default:
                    System.out.println("Unknown Role!");
                    return;
            }
            logDao.logs(loggedIn.user_id(), "LOGIN", null, null, String.format("User %s (ID: %d) logged in.", loggedIn.username(), loggedIn.user_id()));
            menu.show(loggedIn);
            logDao.logs(loggedIn.user_id(), "LOGOUT", null, null, String.format("User %s (ID: %d) logged out.", loggedIn.username(), loggedIn.user_id()));
        }else {
            logDao.logs(null, "LOGIN FAILED", null, null, "FAILED for: " + user);
        }
    }

    private void registerUser(){

        System.out.println("\n    ===== REGISTER =====");

        String full_name;
        while (true) {
            System.out.print("Enter your Fullname: ");
            full_name = sc.nextLine().trim();

            if (full_name.isEmpty()) {
                System.out.println("Invalid! Fullname cannot be empty.");
            } else {
                break;
            }
        }

        String user;
        while (true) {
            System.out.print("Enter username: ");
            user = sc.nextLine().trim();

            if (user.length() < 5) {
                System.out.println("Invalid! Username must be at least 5 characters.");
            }else if (!user.matches("^[a-z0-9_]+$")) {
                System.out.println("Invalid! Use letters, numbers, and underscore only (no spaces).");
            } else if (userDao.usernameExists(user)) {
                System.out.println("Invalid! Username already taken.");
            } else {
                break;
            }
        }

        String pass;
        while (true) {
            System.out.print("Enter Password: ");
            pass = sc.nextLine().trim();

            if (pass.length() < 5) {
                System.out.println("Invalid! Password must be at least 5 characters.");
                continue;
            }

            boolean confirmed = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                System.out.print("Confirm Password: ");
                String confirm = sc.nextLine().trim();

                if (confirm.equals(pass)) {
                    confirmed = true;
                    break;
                }
                System.out.println("\nPasswords do not match! Attempt " + attempt + " of 3.");
            }

            if (confirmed) {
                break;
            }
            System.out.println("Too many wrong attempts. Register Again.");
            return;
        }

        String role;
        while (true) {
            System.out.println("\n-- Select Role --");
            System.out.println("[1] OWNER (admin)");
            System.out.println("[2] CASHIER");
            System.out.println("[3] SYSTEM_AUDITOR");
            System.out.print("Enter Choice: ");
            String roleChoice = sc.nextLine().trim();

            if (roleChoice.equals("1")) {
                role = "ADMIN";
                break;
            } else if (roleChoice.equals("2")) {
                role = "CASHIER";
                break;
            } else if (roleChoice.equals("3")) {
                role = "SYS_AUDITOR";
                break;
            } else {
                System.out.println("Invalid! Please select 1, 2, or 3 only.");
            }
        }

        userDao.registerUser(full_name, user, pass, role);
    }
}
