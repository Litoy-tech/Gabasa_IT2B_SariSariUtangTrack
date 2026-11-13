package com.scc.ui.customer;

import com.scc.dao.*;
import com.scc.model.User;
import com.scc.ui.Menu;

import java.util.Scanner;

import static com.scc.util.InputHelper.readString;

public class CustomerMenu implements Menu{
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final SalesDao salesDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

     public CustomerMenu(CustomerDao customerDao, ProductDao productDao, SalesDao salesDao,
                         AuditLogDao logDao, Scanner sc){
         this.customerDao = customerDao;
         this.productDao = productDao;
         this.salesDao = salesDao;
         this.logDao = logDao;
         this.sc = sc;
     }

     @Override
     public void show(User customer){
         while(true){
             System.out.println("\n===============================");
             System.out.println("         CUSTOMER MENU");
             System.out.println("===============================");
             System.out.println("Welcome, " + customer.fullName() + "!");

             System.out.println("\n[1] My Account\n[2] My Utang Purchases\n[3] My Payments\n[0] Logout");
             String choice = readString(sc, "Enter Choice: ");

             switch(choice){
                 case "1" -> System.out.println("My Account (soon)");
                 case "2" -> System.out.println("My Utang Purchases (soon)");
                 case "3" -> System.out.println("My Payments (soon)");
                 case "0" -> {
                     System.out.println("\nLogging out...");
                     return;
                 }
                 default -> System.out.println(" -> [ERROR] Enter 0-3.");
             }
         }
     }
}
