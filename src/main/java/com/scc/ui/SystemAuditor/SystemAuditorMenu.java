package com.scc.ui.SystemAuditor;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.Menu;

import java.util.Scanner;

public class SystemAuditorMenu implements Menu {
    private final UserDao userDao;
    private final Scanner sc;

    public SystemAuditorMenu(UserDao userdao,Scanner sc) {
        this.userDao = userdao;
        this.sc = sc;
    }

    @Override
    public void show(User u) {
        System.out.println("\n=== SYSTEM AUDITOR MENU ===");
        System.out.println("Welcome, " + u.fullName() + "!");

        while (true) {
            System.out.println("\n[1] Logout");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                System.out.println("You have been logged out.");
                return;
            } else {
                System.out.println("Invalid choice! Please enter 1 only.");
            }
        }
    }
}