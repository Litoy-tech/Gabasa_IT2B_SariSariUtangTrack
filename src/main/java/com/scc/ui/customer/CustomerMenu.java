package com.scc.ui.customer;

import com.scc.dao.*;
import com.scc.model.*;
import com.scc.ui.Menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
             Customer me = findCustomer(customer);
             if(me == null){
                 System.out.println(" -> [ERROR] Customer record not found.");
                 return;
             }
             System.out.println("\n===============================");
             System.out.println("         CUSTOMER MENU");
             System.out.println("===============================");
             System.out.println("Welcome, " + customer.fullName() + "!");

             System.out.println("\n[1] My Account\n[2] My Utang Purchases\n[3] My Payments\n[0] Logout");
             String choice = readString(sc, "Enter Choice: ");

             switch(choice){
                 case "1" -> myAccount(me);
                 case "2" -> myUtang(me);
                 case "3" -> viewMyPayment(me);
                 case "0" -> {
                     System.out.println("\nLogging out...");
                     return;
                 }
                 default -> System.out.println(" -> [ERROR] Enter 0-3.");
             }
         }
     }


     private Customer findCustomer(User u){
         for(Customer c : customerDao.getAllCustomer()){
             if(c.user_id() == u.user_id()){
                 return c;
             }
         }
         return null;
     }

     private void myAccount(Customer me){
         System.out.println("\n===============================");
         System.out.println("          MY ACCOUNT");
         System.out.println("===============================");
         System.out.printf("\nName            : %s", me.full_name());
         System.out.printf("\nContact Number  : %s", me.contact_number());
         System.out.printf("\nAddress         : %s", me.address());
         System.out.printf("\nBalance         : %.2f", me.credit_limit() - me.balance());
         System.out.printf("\nDue Date        : %s", me.due_date() == null ? "-" : me.due_date());
         System.out.printf("\nStatus          : %s", me.status());
         System.out.println("\n===============================");
     }

     private Map<Integer, String> productNames(){
        Map<Integer, String> map = new HashMap<>();
        for(Product p : productDao.getProducts()) map.put(p.product_id(), p.p_name());
        return map;
     }

     private String cut(String text, int max){
         if(text == null) return "-";
         if(text.length() <= max) return text;
         return text.substring(0, max - 3 ) + "...";
     }

     private void myUtang(Customer me){
         Map<Integer, String> names = productNames();

         System.out.println("\n=====================================================================");
         System.out.println("                       MY UTANG PURCHASES");
         System.out.printf(" Balance: ₱ %.2f\n", me.balance());
         System.out.println("\n=====================================================================");
         System.out.printf("| %-19s | %-20s | %-5s | %12s |\n", "Date", "Product", "Qty", "Total");
         System.out.println("=====================================================================");

         boolean found = false;
         double totalUtang = 0;

         for(Sales s: salesDao.getAllSales()){
             if(s.customer_id() != me.customer_id()) continue;
             if(!s.payment_type().equalsIgnoreCase("Utang")) continue;

             for(SalesItem it : salesDao.getSaleItems(s.sales_id())){
                 System.out.printf("| %-19s | %-20s | %5d | %12.2f |\n",
                         cut(s.Sales_date(), 19), cut(names.getOrDefault(it.product_id(), "Uknown"), 20),
                         it.quantity(), it.subtotal());
                 found = true;
                 totalUtang += it.subtotal();
             }
         }

         if(!found){
             System.out.println(" -> No utang purchases yet.");
         }
         System.out.println("=====================================================================");
         if(found){
             System.out.printf(" Total Utang Purchases: ₱ %.2f\n", totalUtang);
         }
     }


     public void viewMyPayment(Customer me){
         List<UtangPayment> paymentList = salesDao.getPaymentCus(me.customer_id());

         System.out.println("\n===========================================");
         System.out.println("             MY PAYMENTS");
         System.out.printf("Current Balance: %.2f", me.balance());
         System.out.println("\n===========================================");

         if(paymentList.isEmpty()){
             System.out.println("\n -> No Payment yet.");
             return;
         }

         System.out.printf("| %-6s | %-15s | %-12s |\n", "ID", "DATE", "AMOUNT");
         System.out.println("===========================================");

         double totalPayment = 0;
         for(UtangPayment p : paymentList){
             System.out.printf("| %-6d | %-15s | %12.2f |\n", p.payment_id(), cut(p.payment_date(), 13), p.amount());
             totalPayment += p.amount();
         }

         System.out.println("===========================================");
         System.out.printf("\bTOTAL PAID: %.2f\n\n", totalPayment);
     }
}
