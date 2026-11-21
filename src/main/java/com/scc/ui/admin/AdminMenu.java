package com.scc.ui.admin;

import com.scc.dao.*;
import com.scc.model.User;
import com.scc.ui.Menu;

import java.util.*;

import static com.scc.util.InputHelper.readInt;

public class AdminMenu implements Menu {
    private final UserDao userdao;
    private final ProductDao productDao;
    private final CustomerDao customerDao;
    private final SalesDao salesDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

    public AdminMenu(UserDao userdao, CustomerDao customerDao, ProductDao productDao, SalesDao salesDao, AuditLogDao logDao, Scanner sc){
        this.userdao = userdao;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.salesDao = salesDao;
        this.logDao = logDao;
        this.sc = sc;
    }

    @Override
    public void show(User admin){
        while(true){
            System.out.println("\n===============================");
            System.out.println("            OWNER MENU ");
            System.out.println("        Welcome, " + admin.fullName() + "!");
            System.out.println("===============================");
            System.out.println("[1] Manage Product\n[2] Manage Customers\n[3] Manage Users\n[4] View All Sales" +
                    "\n[5] Best Sale\n[6] Reports\n[7] Logout");
            int choice = readInt(sc, "Enter Choice: ");

            switch(choice){
                case 1 -> new ProductManagement(productDao, logDao,sc).show(admin);
                case 2 -> new CustomerManagement(userdao, customerDao, logDao, sc).show(admin);
                case 3 -> new UserManagement(userdao, logDao,sc).show(admin);
                case 4 -> new SalesReport(userdao, customerDao, productDao, salesDao, sc).viewSales();
                case 5 -> new SalesReport(userdao, customerDao, productDao, salesDao, sc).bestSales();
                case 6 -> new SalesReport(userdao, customerDao, productDao, salesDao, sc).reportMenu(admin);
                case 7 ->{
                    System.out.println("\nLogging out..."); return;
                }
                default -> System.out.println("Invalid choice! Pili ra gikan sa 1 hangtod 7.");
            }
        }
    }
}