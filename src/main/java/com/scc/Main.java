package com.scc;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.Cashier.CashierMenu;
import com.scc.ui.Menu;
import com.scc.ui.Owner.OwnerMenu;
import com.scc.ui.SystemAuditor.SystemAuditorMenu;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(){

        while(true){

            System.out.println("\n========================================");
            System.out.println("              SARI SARI STORE");
            System.out.println("\n========================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice){
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

    public static void userLogin(){
        System.out.println("\n    ===== LOG IN =====");

        String user;
        while (true) {
            System.out.print("Enter Username: ");
            user = sc.nextLine().trim();

            if(user.isEmpty()){
                System.out.println("Invalid! Fullname cannot be empty.");
            }else {
                break;
            }
        }

        String pass;
        while (true) {
            System.out.print("Enter Password: ");
            pass = sc.nextLine().trim();

            if(pass.isEmpty()){
                System.out.println("Invalid! Fullname cannot be empty.");
            }else{
                break;
            }
        }

        User loggedIn = userDao.login(user, pass);

        if(loggedIn != null){
            Menu menu;
            switch (loggedIn.roles()){
                case "OWNER":
                    menu = new OwnerMenu( userDao,sc);
                    break;
                case "CASHIER":
                    menu = new CashierMenu(userDao,sc);
                    break;
                case "SYS_AUDITOR":
                    menu = new SystemAuditorMenu(userDao,sc);
                    break;
                default:
                    System.out.println("Unknown Role!");
                    return;
            }
            menu.show(loggedIn);
        }
    }

    public static void registerUser(){

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
            user = sc.nextLine().trim().toLowerCase();

            if (!user.endsWith("@gmail.com") || user.length() <= "@gmail.com".length() || user.contains(" ")) {
                System.out.println("Invalid! Enter a valid Gmail without spaces (example: lito@gmail.com).");
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
                role = "OWNER";
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
