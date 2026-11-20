package com.scc.ui.cashier;

import com.scc.dao.*;
import com.scc.model.User;
import com.scc.util.InputHelper;
import com.scc.ui.Menu;

import java.util.Scanner;

public class CashierMenu implements Menu {
    private final CashierService service;
    private final Scanner sc;

    public CashierMenu(UserDao userDao, ProductDao productDao, CustomerDao customerDao, SalesDao salesDao, AuditLogDao logDao, Scanner sc) {
        this.service = new CashierService(productDao, customerDao, salesDao, logDao, sc);
        this.sc = sc;
    }

    @Override
    public void show(User cashier) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("          CASHIER MENU");
            System.out.println("   Welcome, " + cashier.fullName() + "!");
            System.out.println("===============================");
            System.out.println();
            System.out.println("  [1] Process Sale");
            System.out.println("  [2] Record Utang Payment");
            System.out.println("  [3] Search Customer");
            System.out.println("  [4] View Product List");
            System.out.println("  [5] Logout");
            System.out.println("-------------------------------");

            int choice = InputHelper.readInt(sc, "Enter Choice: ");

            switch (choice) {
                case 1:
                    boolean again;
                    do {
                        again = service.processSale(cashier);
                    } while (again && InputHelper.readYesNo(sc, "\nProcess another sale?"));
                    break;
                case 2:
                    service.recordUtangPayment(cashier);
                    break;
                case 3:
                    service.searchCustomer();
                    break;
                case 4:
                    service.viewProducts();
                    break;
                case 5:
                    System.out.println("\n -> Logging out...");
                    return;
                default:
                    System.out.println("\n -> Invalid choice! Pili ra gikan sa 1 hangtod 5.");
            }
        }
    }
}