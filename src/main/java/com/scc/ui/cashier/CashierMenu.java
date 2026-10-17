package com.scc.ui.cashier;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.InputHelper;
import com.scc.ui.Menu;

import java.util.Scanner;

public class CashierMenu implements Menu {
    private final UserDao userDao;
    private final Scanner sc;

    public CashierMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    @Override
    public void show(User cashier) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("          CASHIER MENU");
            System.out.println("   Welcome, " + cashier.fullName() + "!");
            System.out.println("===============================");
            System.out.println("[1] Process Sale\n[2] Record Utang Payment\n[3] Search Customer\n[4] View Product List\n[5] Logout");

            int choice = InputHelper.readInt(sc, "Enter Choice: ");

            switch (choice) {
                case 1:
                    System.out.println("Process Sale (soon)");
                    break;
                case 2:
                    System.out.println("Record Utang Payment (soon)");
                    break;
                case 3:
                    System.out.println("Search Customer (soon)");
                    break;
                case 4:
                    System.out.println("View Product List (soon)");
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice! Pili ra gikan sa 1 hangtod 5.");
            }
        }
    }
}