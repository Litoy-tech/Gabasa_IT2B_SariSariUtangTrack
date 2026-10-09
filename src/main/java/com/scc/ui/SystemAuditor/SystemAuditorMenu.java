package com.scc.ui;

import com.scc.model.User;

import java.util.Scanner;

public class SystemAuditorMenu {
    private final Scanner sc;

    public SystemAuditorMenu(Scanner sc) {
        this.sc = sc;
    }

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