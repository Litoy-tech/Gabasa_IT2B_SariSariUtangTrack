package com.scc.ui.systemAuditor;

import com.scc.dao.AuditLogDao;
import com.scc.dao.CustomerDao;
import com.scc.dao.ProductDao;
import com.scc.dao.SalesDao;
import com.scc.dao.UserDao;
import com.scc.model.Audit;
import com.scc.model.Customer;
import com.scc.model.Product;
import com.scc.model.Sales;
import com.scc.model.SalesItem;
import com.scc.model.User;
import com.scc.ui.Menu;
import com.scc.util.InputHelper;

import static com.scc.util.InputHelper.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SystemAuditorMenu implements Menu {
    private final UserDao userDao;
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final SalesDao salesDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

    public SystemAuditorMenu(UserDao userDao, CustomerDao customerDao, ProductDao productDao,
                             SalesDao salesDao, AuditLogDao logDao, Scanner sc) {
        this.userDao = userDao;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.salesDao = salesDao;
        this.logDao = logDao;
        this.sc = sc;
    }

    @Override
    public void show(User SysAuditor) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("     SYSTEM AUDITOR MENU");
            System.out.println("===============================");
            System.out.println("Welcome, " + SysAuditor.fullName() + "!");

            System.out.println("\n[1] View Audit Log\n[2] View Sales\n[3] Daily Sales Report\n[4] Utang Report\n" +
                    "[5] Export Reports (PDF / CSV)\n" +
                    "[0] Logout");
            String choice = readString(sc, "Enter Choice: ");

            switch(choice){
                case "1" -> logHistory();
                case "2" -> viewSales();
                case "3" -> System.out.println("Daily Sales Report (soon)");
                case "4" -> System.out.println("Utang Report (soon)");
                case "5" -> System.out.println("Export Reports (soon)");
                case "0" -> {return;}
                default -> System.out.println(" Enter 0-5.");
            }
        }
    }

    // ===================== AUDIT LOG =====================

    private boolean containsIgnoreCase(String text, String keyword){
        return text != null && text.toLowerCase().contains(keyword.toLowerCase());
    }

    private boolean matchesCategory(Audit a, String category){
        String table = (a.table_affected() == null) ? "" : a.table_affected();
        return switch (category){
            case "LOGIN / LOGOUT" -> a.action().startsWith("LOGIN") || a.action().equals("LOGOUT");
            case "PRODUCTS" -> table.equals("product_tbl");
            case "USERS" -> table.equals("users");
            case "CUSTOMERS" -> table.equals("customer_tbl");
            case "SALES" -> table.equals("sales") || table.equals("utang_payments");
            default -> true;
        };
    }

    private List<Audit> filterLogs(String category, Integer userId){
        return logDao.getAllLogs().stream()
                .filter(a -> category == null || matchesCategory(a, category))
                .filter(a -> userId == null || userId.equals(a.user_id()))
                .toList();
    }

    private void logHistory(){
        String category = null;
        Integer userId = null;

        while (true){
            String title = (category == null) ? "ALL AUDIT LOGS" : category + " LOGS";
            if(userId != null) title += " (user ID: " + userId + ")";
            displayLogTable(filterLogs(category, userId), title);

            String choice = readString(sc, "Filter ([1] ALL | [2] LOGIN/LOGOUT | [3] PRODUCTS | [4] USERS | [5] CUSTOMERS | [6] SALES | [7] Filter by User ID | [0] Back): ").trim();

            switch (choice){
                case "1" -> {category = null; userId = null;}
                case "2" -> category = "LOGIN / LOGOUT";
                case "3" -> category = "PRODUCTS";
                case "4" -> category = "USERS";
                case "5" -> category = "CUSTOMERS";
                case "6" -> category = "SALES";
                case "7" -> userId = readInt(sc, "Enter user ID: ");
                case "0" -> {return;}
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 7 only.");
            }
        }
    }

    private void displayLogTable(List<Audit> logs, String title){
        System.out.println("\n===========================================================================================================================================================");
        System.out.println("                                                         " + title + " ");
        if(logs.isEmpty()){
            System.out.println("\n-> No Records.");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        System.out.println("+------+---------------------+------+------------------+------------------------+--------------+--------+-------------------------------------------------+");
        System.out.printf ("| %-4s | %-19s | %-4s | %-16s | %-22s | %-12s | %-6s | %-47s |\n", "ID", "Timestamp", "UID", "User", "Action", "Table", "Rec ID", "Description");
        System.out.println("+------+---------------------+------+------------------+------------------------+--------------+--------+-------------------------------------------------+");
        for(Audit a : logs){
            String uid = (a.user_id() == null) ? "-" : String.valueOf(a.user_id());
            String table = (a.table_affected() == null) ? "-" : a.table_affected();
            String recId = (a.record_id() == null) ? "-" : String.valueOf(a.record_id());
            System.out.printf("| %-4d | %-19s | %-4s | %-16s | %-22s | %-12s | %-6s | %-47s |\n",
                    a.log_id(), a.log_stamp(), uid, cut(a.username(), 16), cut(a.action(), 22), cut(table, 12), recId, cut(a.description(), 47));
        }
        System.out.println("+------+---------------------+------+------------------+------------------------+--------------+--------+-------------------------------------------------+");
        System.out.println(" Total logs: " + logs.size());
    }

    // ===================== VIEW SALES (read-only) =====================

    private Map<Integer, String> cashierNames(){
        Map<Integer, String> map = new HashMap<>();
        for(User u : userDao.getAllUser()){
            map.put(u.user_id(), u.username());
        }
        return map;
    }

    private Map<Integer, String> customerNames(){
        Map<Integer, String> map = new HashMap<>();
        for(Customer c : customerDao.getAllCustomer()){
            map.put(c.customer_id(), c.full_name());
        }
        return map;
    }

    private Map<Integer, String> productNames(){
        Map<Integer, String> map = new HashMap<>();
        for(Product p : productDao.getProducts()){
            map.put(p.product_id(), p.p_name());
        }
        return map;
    }

    private List<Sales> filterSales(String type, String keyword){
        Map<Integer, String> cashiers = cashierNames();
        Map<Integer, String> customers = customerNames();

        return salesDao.getAllSales().stream()
                .filter(s -> type == null || s.payment_type().equalsIgnoreCase(type))
                .filter(s -> keyword == null
                        || String.valueOf(s.sales_id()).equals(keyword)
                        || containsIgnoreCase(cashiers.get(s.cashier_id()), keyword)
                        || containsIgnoreCase(customers.get(s.customer_id()), keyword))
                .toList();
    }

    public void viewSales(){
        String type = null;
        String keyword = null;

        while (true){
            String title = (type == null) ? "ALL SALES" : type.toUpperCase() + " SALES";
            if(keyword != null) title += " (search: " + keyword + ")";
            displaySalesTable(filterSales(type, keyword), title);

            String choice = readString(sc, "Filter ([1] ALL | [2] CASH | [3] UTANG | [4] Search | [5] View Sale Details | [0] Back): ").trim();

            switch (choice){
                case "1" -> {type = null; keyword = null;}
                case "2" -> type = "Cash";
                case "3" -> type = "Utang";
                case "4" -> keyword = readString(sc, "Enter Sale ID, cashier or customer name: ");
                case "5" -> viewSaleDetails();
                case "0" -> {return;}
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 5 only.");
            }
        }
    }

    private void displaySalesTable(List<Sales> salesList, String title){
        System.out.println("\n=====================================================================================================");
        System.out.println("                                           " + title + " ");
        if(salesList.isEmpty()){
            System.out.println("\n-> No Records.");
            System.out.println("-----------------------------------------------------------------------------------------------------");
            return;
        }

        Map<Integer, String> cashiers = cashierNames();
        Map<Integer, String> customers = customerNames();
        double grandTotal = 0;

        System.out.println("+------+---------------------+------------------+---------+--------------------------+--------------+");
        System.out.printf ("| %-4s | %-19s | %-16s | %-7s | %-24s | %12s |\n", "ID", "Date", "Cashier", "Type", "Customer", "Total");
        System.out.println("+------+---------------------+------------------+---------+--------------------------+--------------+");
        for(Sales s : salesList){
            String cashier = cashiers.getOrDefault(s.cashier_id(), "(unknown)");
            String customer = (s.customer_id() == 0) ? "-" : customers.getOrDefault(s.customer_id(), "(unknown)");
            System.out.printf("| %-4d | %-19s | %-16s | %-7s | %-24s | %12.2f |\n",
                    s.sales_id(), s.Sales_date(), cut(cashier, 16), s.payment_type(), cut(customer, 24), s.total_amount());
            grandTotal += s.total_amount();
        }
        System.out.println("+------+---------------------+------------------+---------+--------------------------+--------------+");
        System.out.printf(" Total sales: %d | Total amount: ₱ %.2f\n", salesList.size(), grandTotal);
    }

    public void viewSaleDetails(){
        int id = readInt(sc, "Enter Sale ID (0 to cancel): ");
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


    private String cut(String text, int max){
        if(text == null) return "-";
        if(text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}