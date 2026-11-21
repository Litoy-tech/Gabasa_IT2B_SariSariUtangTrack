package com.scc.ui.systemAuditor;

import com.scc.dao.*;
import com.scc.model.*;
import com.scc.util.ReportExporter;

import java.time.LocalDate;
import java.util.*;

import static com.scc.util.InputHelper.*;

public class SystemAuditorService {
    private final UserDao userDao;
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final SalesDao salesDao;
    private final AuditLogDao logDao;
    private final ReportExporter exporter;
    private final Scanner sc;

    public SystemAuditorService(UserDao userDao, CustomerDao customerDao, ProductDao productDao,
                                SalesDao salesDao, AuditLogDao logDao, Scanner sc) {
        this.userDao = userDao;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.salesDao = salesDao;
        this.logDao = logDao;
        this.exporter = new ReportExporter(userDao, customerDao);
        this.sc = sc;
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

    public void logHistory(){
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

            String choice = readString(sc, "\nFilter ([1] ALL | [2] CASH | [3] UTANG | [4] Search | [5] View Sale Details | [0] Back): ").trim();

            switch (choice){
                case "1" -> {type = null; keyword = null;}
                case "2" -> type = "Cash";
                case "3" -> type = "Utang";
                case "4" -> keyword = readString(sc, "Enter Sale ID, cashier or customer name: ");
                case "5" -> {
                    viewSaleDetails();
                    System.out.println("\nPress ENTER...");
                    sc.nextLine();
                }
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

        System.out.println("\n====================================================================");
        System.out.println("                            SALE DETAILS ");
        System.out.println("--------------------------------------------------------------------");
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

    // ===================== DAILY SALES REPORT =====================

    public void dailySalesReport(){
        String date = readString(sc, "\nEnter date (YYYY-MM-DD), T = today, 0 = cancel: ").trim();
        if(date.equals("0")) return;
        if(date.equalsIgnoreCase("T")) date = LocalDate.now().toString();

        if(!date.matches("\\d{4}-\\d{2}-\\d{2}")){
            System.out.println(" -> [ERROR] Use format YYYY-MM-DD.");
            return;
        }

        final String day = date;
        List<Sales> list = salesDao.getAllSales().stream()
                .filter(s -> s.Sales_date() != null && s.Sales_date().startsWith(day))
                .toList();

        displaySalesTable(list, "DAILY SALES REPORT: " + day);

        if(!list.isEmpty()){
            String ans = readString(sc, "\nExport to CSV? (Y/N): ").trim();
            if(ans.equalsIgnoreCase("Y")){
                exporter.exportDailySales(list, day);
            }
        }
    }

    // ===================== UTANG REPORT =====================

    private String dueStatus(String due){
        if(due == null || due.isBlank()) return "-";
        try{
            return LocalDate.parse(due).isBefore(LocalDate.now()) ? "OVERDUE" : "Current";
        } catch (Exception e) {
            return "-";
        }
    }


    public void utangReport(){
        List<Customer> all = customerDao.getAllCustomer();
        List<Customer> utangList = new ArrayList<>();

        for(Customer c : all){
            if(c.balance() > 0){
                utangList.add(c);
            }
        }

        if(utangList.isEmpty()){
            System.out.println("\n -> No customers with utang.");
            return;
        }

        utangList.sort((a, b) -> Double.compare(b.balance(), a.balance()));

        System.out.println("\n================================================================================");
        System.out.println("                                  UTANG REPORT");
        System.out.println("================================================================================");
        System.out.println("+------+----------------------+------------+------------+------------+---------+");
        System.out.printf("| %-4s | %-20s | %10s | %10s | %-10s | %-7s |\n",
                "ID", "Name", "Balance", "Limit", "Due Date", "Status");
        System.out.println("+------+----------------------+------------+------------+------------+---------+");

        double totalOutstanding = 0;
        int overDue = 0;
        for(Customer c : utangList){
            String status = dueStatus(c.due_date());
            if(status.equals("OVERDUE")) overDue++;

            System.out.printf("| %-4d | %-20s | %10.2f | %10.2f | %-10s | %-7s |\n", c.customer_id(), cut(c.full_name(), 20),
                    c.balance(), c.credit_limit(), c.due_date() == null ? "-" : c.due_date(), status);
            totalOutstanding += c.balance();
        }

        System.out.println("+------+----------------------+------------+------------+------------+---------+");
        System.out.printf(" Customers with utang : %d | OVERDUE : %d | Total Outstanding : ₱ %.2f\n",
                utangList.size(), overDue, totalOutstanding);

        String ans = readString(sc, "\nExport to CSV? (Y/N): ").trim();
        if(ans.equalsIgnoreCase("y")){
            exporter.exportUtang(utangList);
        }
        while(true){
            int id = readInt(sc, "\nEnter Customer ID to see details (0 to cancel): ");

            if(id == 0) return;

            Customer target = null;
            for(Customer c : utangList){
                if(c.customer_id() == id){
                    target = c;
                    break;
                }
            }

            if(target == null){
                System.out.println(" -> Customer not found in the utang list.");
                continue;
            }
            utangDetails(target);
        }
    }

    public void utangDetails(Customer c){
        Map<Integer, String> names = productNames();
        System.out.println("\n=====================================================================");
        System.out.println(" UTANG DETAILS: " + c.full_name());
        System.out.printf(" Balance: ₱ %.2f | Due Date: %s\n", c.balance(), c.due_date() == null ? "-" : c.due_date());
        System.out.println("=====================================================================");
        System.out.printf("| %-19s | %-20s | %-5s | %12s |\n", "Date", "Product", "Qty", "Total");
        System.out.println("=====================================================================");

        boolean found = false;

        for(Sales s : salesDao.getAllSales()){
            if(s.customer_id() != c.customer_id()) continue;
            if(!s.payment_type().equalsIgnoreCase("Utang")) continue;

            for(SalesItem it: salesDao.getSaleItems(s.sales_id())){
                System.out.printf("| %-19s | %-20s | %-5d | %12.2f |\n", cut(s.Sales_date(), 19), cut(names.getOrDefault(it.product_id(), "Unknown"), 20),
                        it.quantity(), it.subtotal());
                found = true;
            }
        }

        if(!found) System.out.println(" -> No Utang purchases found.");
        System.out.println("=====================================================================");

        // ---- PAYMENTS (full / partial) ----
        List<UtangPayment> payments = salesDao.getPaymentCus(c.customer_id());
        Map<Integer, String> staff = cashierNames();

        System.out.println("\n PAYMENTS: " + c.full_name());
        System.out.println("=====================================================================");
        System.out.printf("| %-6s | %-19s | %12s | %-19s |\n", "Pay #", "Date", "Amount", "Received By");
        System.out.println("=====================================================================");

        double totalPaid = 0;
        for(UtangPayment p : payments){
            System.out.printf("| %-6d | %-19s | %12.2f | %-19s |\n",
                    p.payment_id(), cut(p.payment_date(), 19), p.amount(),
                    cut(staff.getOrDefault(p.received_by(), "(unknown)"), 19));
            totalPaid += p.amount();
        }

        if(payments.isEmpty()) System.out.println(" -> No payments yet.");
        else System.out.printf(" Payments made: %d | Total paid: ₱ %.2f\n", payments.size(), totalPaid);
        System.out.println("=====================================================================");
    }

    // ===================== VIEW PAYMENTS =====================

    public void viewPayments(){
        String keyword = readString(sc, "\nEnter customer name (A = all, 0 = cancel): ").trim();
        if(keyword.equals("0")) return;
        boolean showAll = keyword.equalsIgnoreCase("A");

        Map<Integer, String> staff = cashierNames();
        double totalPaid = 0;
        int count = 0;

        System.out.println("\n=========================================================================================");
        System.out.println("                                     UTANG PAYMENTS");
        System.out.println("=========================================================================================");
        System.out.printf("| %-6s | %-19s | %-24s | %12s | %-12s |\n",
                "Pay #", "Date", "Customer", "Amount", "Received By");
        System.out.println("=========================================================================================");

        for(Customer c : customerDao.getAllCustomer()){
            if(!showAll && !containsIgnoreCase(c.full_name(), keyword)) continue;

            for(UtangPayment p : salesDao.getPaymentCus(c.customer_id())){
                System.out.printf("| %-6d | %-19s | %-24s | %12.2f | %-12s |\n",
                        p.payment_id(), cut(p.payment_date(), 19), cut(c.full_name(), 24),
                        p.amount(), cut(staff.getOrDefault(p.received_by(), "(unknown)"), 16));
                totalPaid += p.amount();
                count++;
            }

        }
        System.out.println("-----------------------------------------------------------------------------------------");
        if(count == 0) System.out.println(" -> No payments found.");
        else System.out.printf("\n Payments made: %d | Total paid: ₱ %.2f\n", count, totalPaid);
        System.out.println("=========================================================================================");

        pause();
    }

    private void pause(){
        System.out.print("\nPress ENTER to continue...");
        sc.nextLine();
    }

}