package com.scc.ui.admin;

import com.scc.dao.AuditLogDao;
import com.scc.dao.CustomerDao;
import com.scc.dao.ProductDao;
import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.util.InputHelper;
import com.scc.ui.Menu;

import java.util.Scanner;

public class AdminMenu implements Menu {
    private final UserDao userdao;
    private final ProductDao productDao;
    private final CustomerDao customerDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

    public AdminMenu(UserDao userdao,CustomerDao customerDao, ProductDao productDao, AuditLogDao logDao,Scanner sc){
        this.userdao = userdao;
        this.customerDao = customerDao;
        this.productDao = productDao;
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
            System.out.println("[1] Manage Product\n[2] Manage Customers\n[3] Manage Users\n[4] View Sales" +
                    "\n[5] Reports\n[6] Logout");
            int choice = InputHelper.readInt(sc, "Enter Choice: ");

            switch(choice){
                case 1 -> new ProductManagement(productDao, logDao,sc).show(admin);
                case 2 -> new CustomerManagement(userdao, customerDao, logDao, sc).show(admin);
                case 3 -> new UserManagement(userdao, logDao,sc).show(admin);
                case 4 -> System.out.println("View Sales (soon)");
                case 5 -> System.out.println("Reports (soon)");
                case 6 ->{
                    System.out.println("\nLogging out..."); return;
                }
                default -> System.out.println("Invalid choice! Pili ra gikan sa 1 hangtod 6.");
            }
        }
    }
}
