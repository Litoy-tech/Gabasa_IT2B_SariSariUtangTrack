package com.scc.ui.admin;

import com.scc.dao.*;
import com.scc.model.Customer;
import com.scc.model.Product;
import com.scc.model.Sales;
import com.scc.model.SalesItem;
import com.scc.model.User;
import com.scc.util.InputHelper;
import com.scc.ui.Menu;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
            System.out.println("[1] Manage Product\n[2] Manage Customers\n[3] Manage Users\n[4] View Sales" +
                    "\n[5] Reports\n[6] Logout");
            int choice = InputHelper.readInt(sc, "Enter Choice: ");

            switch(choice){
                case 1 -> new ProductManagement(productDao, logDao,sc).show(admin);
                case 2 -> new CustomerManagement(userdao, customerDao, logDao, sc).show(admin);
                case 3 -> new UserManagement(userdao, logDao,sc).show(admin);
                case 4 -> viewSales();
                case 5 -> System.out.println("Reports (soon)");
                case 6 ->{
                    System.out.println("\nLogging out..."); return;
                }
                default -> System.out.println("Invalid choice! Pili ra gikan sa 1 hangtod 6.");
            }
        }
    }

    // ===================== VIEW SALES (Owner) =====================

    private Map<Integer, String> cashierNames(){
        Map<Integer, String> map = new HashMap<>();
        for(User u : userdao.getAllUser()) map.put(u.user_id(), u.username());
        return map;
    }

    private Map<Integer, String> customerNames(){
        Map<Integer, String> map = new HashMap<>();
        for(Customer c : customerDao.getAllCustomer()) map.put(c.customer_id(), c.full_name());
        return map;
    }

    private Map<Integer, String> productNames(){
        Map<Integer, String> map = new HashMap<>();
        for(Product p : productDao.getProducts()) map.put(p.product_id(), p.p_name());
        return map;
    }

    private boolean containsIgnoreCase(String text, String keyword){
        return text != null && text.toLowerCase().contains(keyword.toLowerCase());
    }

    private String cut(String text, int max){
        if(text == null) return "-";
        if(text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }

    private List<Sales> filterSales(String type, String keyword, String date){
        Map<Integer, String> cashiers = cashierNames();
        Map<Integer, String> customers = customerNames();

        return salesDao.getAllSales().stream()
                .filter(s -> type == null || s.payment_type().equalsIgnoreCase(type))
                .filter(s -> date == null || (s.Sales_date() != null && s.Sales_date().startsWith(date)))
                .filter(s -> keyword == null
                        || String.valueOf(s.sales_id()).equals(keyword)
                        || containsIgnoreCase(cashiers.get(s.cashier_id()), keyword)
                        || containsIgnoreCase(customers.get(s.customer_id()), keyword))
                .toList();
    }

    private void viewSales(){
        String type = null;
        String keyword = null;
        String date = null;

        while (true){
            String title = (type == null) ? "ALL SALES" : type.toUpperCase() + " SALES";
            if(date != null) title += " (date: " + date + ")";
            if(keyword != null) title += " (search: " + keyword + ")";
            displaySalesTable(filterSales(type, keyword, date), title);

            String choice = InputHelper.readString(sc, "\nFilter ([1] ALL | [2] CASH | [3] UTANG | [4] TODAY | [5] DATE | [6] Search | [7] Sale Details | [0] Back): ").trim();

            switch (choice){
                case "1" -> {type = null; keyword = null; date = null;}
                case "2" -> type = "Cash";
                case "3" -> type = "Utang";
                case "4" -> date = LocalDate.now().toString();
                case "5" -> {
                    String d = InputHelper.readString(sc, "Enter date (YYYY-MM-DD) or 0 to clear: ").trim();
                    if(d.equals("0")) date = null;
                    else if(d.matches("\\d{4}-\\d{2}-\\d{2}")) date = d;
                    else System.out.println(" -> [ERROR] Use format YYYY-MM-DD.");
                }
                case "6" -> keyword = InputHelper.readString(sc, "Enter Sale ID, cashier or customer name: ").trim();
                case "7" -> viewSaleDetails();
                case "0" -> {return;}
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 7 only.");
            }
        }
    }

    private void displaySalesTable(List<Sales> salesList, String title){
        System.out.println("\n=====================================================================================================");
        System.out.println("                                        " + title + " ");
        if(salesList.isEmpty()){
            System.out.println("\n-> No Records.");
            System.out.println("-----------------------------------------------------------------------------------------------------");
            return;
        }

        Map<Integer, String> cashiers = cashierNames();
        Map<Integer, String> customers = customerNames();
        double grandTotal = 0, cashTotal = 0, utangTotal = 0;
        int cashCount = 0, utangCount = 0;

        System.out.println("+------+---------------------+------------------+---------+--------------------------+--------------+");
        System.out.printf ("| %-4s | %-19s | %-16s | %-7s | %-24s | %12s |\n", "ID", "Date", "Cashier", "Type", "Customer", "Total");
        System.out.println("+------+---------------------+------------------+---------+--------------------------+--------------+");
        for(Sales s : salesList){
            String cashier = cashiers.getOrDefault(s.cashier_id(), "(unknown)");
            String customer = (s.customer_id() == 0) ? "-" : customers.getOrDefault(s.customer_id(), "(unknown)");
            System.out.printf("| %-4d | %-19s | %-16s | %-7s | %-24s | %12.2f |\n",
                    s.sales_id(), s.Sales_date(), cut(cashier, 16), s.payment_type(), cut(customer, 24), s.total_amount());
            grandTotal += s.total_amount();
            if(s.payment_type().equalsIgnoreCase("Utang")){
                utangTotal += s.total_amount();
                utangCount++;
            }else {
                cashTotal += s.total_amount();
                cashCount++;
            }
        }
        System.out.println("+------+---------------------+------------------+---------+--------------------------+--------------+");
        System.out.printf(" Total sales: %d | Total amount: ₱ %.2f\n", salesList.size(), grandTotal);
        System.out.printf(" Cash : %d sale(s) | ₱ %.2f\n", cashCount, cashTotal);
        System.out.printf(" Utang: %d sale(s) | ₱ %.2f\n", utangCount, utangTotal);
    }

    private void viewSaleDetails(){
        int id = InputHelper.readInt(sc, "Enter Sale ID (0 to cancel): ");
        if(id == 0){
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        Sales sale = salesDao.getAllSales().stream().filter(s -> s.sales_id() == id).findFirst().orElse(null);
        if(sale == null){
            System.out.println(" -> [ERROR] Sale ID not found.");
            return;
        }

        String cashier = cashierNames().getOrDefault(sale.cashier_id(), "(unknown)");
        String customer = (sale.customer_id() == 0) ? "-" : customerNames().getOrDefault(sale.customer_id(), "(unknown)");
        Map<Integer, String> products = productNames();
        List<SalesItem> items = salesDao.getSaleItems(id);

        System.out.println("\n=========================== SALE DETAILS ===========================");
        System.out.println("Sale #     : " + sale.sales_id());
        System.out.println("Date       : " + sale.Sales_date());
        System.out.println("Cashier    : " + cashier);
        System.out.println("Payment    : " + sale.payment_type());
        System.out.println("Customer   : " + customer);

        System.out.println("+--------------------------------+-------+------------+------------+");
        System.out.printf ("| %-30s | %5s | %10s | %10s |\n", "Product", "Qty", "Unit Price", "Subtotal");
        System.out.println("+--------------------------------+-------+------------+------------+");
        for(SalesItem it : items){
            String name = products.getOrDefault(it.product_id(), "(deleted product)");
            System.out.printf("| %-30s | %5d | %10.2f | %10.2f |\n", cut(name, 30), it.quantity(), it.unit_price(), it.subtotal());
        }
        System.out.println("+--------------------------------+-------+------------+------------+");

        System.out.printf("Total      : ₱ %.2f\n", sale.total_amount());
        if(sale.payment_type().equalsIgnoreCase("Cash")){
            System.out.printf("Received   : ₱ %.2f\n", sale.amount_received());
            System.out.printf("Change     : ₱ %.2f\n", sale.change_amount());
        }
    }
}