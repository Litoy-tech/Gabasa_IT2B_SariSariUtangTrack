package com.scc.ui.customer;

import com.scc.dao.*;
import com.scc.model.*;
import com.scc.ui.Menu;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.scc.util.InputHelper.readInt;
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
            List<Note> notes = getNotifications(me);
            String notifLabel = notes.isEmpty() ? "" : " (" + notes.size() + ")";

            System.out.println("\n===============================");
            System.out.println("         CUSTOMER MENU");
            System.out.println("===============================");
            System.out.println("Welcome, " + customer.fullName() + "!");

            System.out.println("\n[1] My Account\n[2] View My Record\n[3] Notification" + notifLabel + "\n[0] Logout");
            String choice = readString(sc, "Enter Choice: ");

            switch(choice){
                case "1" -> myAccount(me);
                case "2" -> viewMyRecord(me);
                case "3" -> viewNotifications(notes);
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

    // ===================== NOTIFICATIONS =====================

    private record Note(String date, String type, String message, List<String> details){}

    private List<Note> getNotifications(Customer me){
        List<Note> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Overdue / due soon
        String due = me.due_date();
        if(due != null && !due.isBlank() && me.balance() > 0){
            try{
                long days = ChronoUnit.DAYS.between(today, LocalDate.parse(due));
                if(days < 0){
                    List<String> d = new ArrayList<>();
                    d.add("Due Date      : " + due);
                    d.add("Days Overdue  : " + (-days));
                    d.add(String.format("Balance       : ₱ %.2f", me.balance()));
                    d.add("Please pay at the store as soon as possible.");
                    list.add(new Note(due, "OVERDUE", "Your utang is past its due date.", d));
                }else if(days <= 3){
                    List<String> d = new ArrayList<>();
                    d.add("Due Date      : " + due);
                    d.add("Days Left     : " + days);
                    d.add(String.format("Balance       : ₱ %.2f", me.balance()));
                    list.add(new Note(due, "REMINDER", "Your utang is due soon.", d));
                }
            }catch (Exception e){
                // wrong date format, ignore
            }
        }

        // 2. Utang taken today
        Map<Integer, String> names = productNames();
        for(Sales s : salesDao.getAllSales()){
            if(s.customer_id() != me.customer_id()) continue;
            if(!s.payment_type().equalsIgnoreCase("Utang")) continue;
            if(s.Sales_date() == null || !s.Sales_date().startsWith(today.toString())) continue;

            List<String> d = new ArrayList<>();
            d.add("Sale #        : " + s.sales_id());
            d.add("Date          : " + s.Sales_date());
            d.add(String.format("%-24s %5s %10s %10s", "Product", "Qty", "Price", "Subtotal"));
            for(SalesItem it : salesDao.getSaleItems(s.sales_id())){
                d.add(String.format("%-24s %5d %10.2f %10.2f",
                        cut(names.getOrDefault(it.product_id(), "Unknown"), 24), it.quantity(), it.unit_price(), it.subtotal()));
            }
            d.add(String.format("Total         : ₱ %.2f", s.total_amount()));
            list.add(new Note(s.Sales_date(), "UTANG", String.format("You took utang: ₱ %.2f", s.total_amount()), d));
        }

        // 3. Payments made today
        for(UtangPayment p : salesDao.getPaymentCus(me.customer_id())){
            if(p.payment_date() == null || !p.payment_date().startsWith(today.toString())) continue;

            String dueText = (due == null || due.isBlank() || me.balance() <= 0) ? "-" : due;
            List<String> d = new ArrayList<>();
            d.add("Payment #     : " + p.payment_id());
            d.add("Date          : " + p.payment_date());
            d.add(String.format("Amount Paid   : ₱ %.2f", p.amount()));
            d.add(String.format("New Balance   : ₱ %.2f", me.balance()));
            d.add("Due Date      : " + dueText);
            list.add(new Note(p.payment_date(), "PAYMENT", String.format("You paid: ₱ %.2f", p.amount()), d));
        }
        return list;
    }

    private void viewNotifications(List<Note> notes){
        while(true){
            System.out.println("\n============================================================================");
            System.out.println("                              NOTIFICATIONS");
            System.out.println("============================================================================");
            if(notes.isEmpty()){
                System.out.println(" -> No notifications.");
                return;
            }

            System.out.printf("| %-3s | %-19s | %-8s | %-33s |\n", "#", "Date", "Type", "Message");
            System.out.println("----------------------------------------------------------------------------");
            for(int i = 0; i < notes.size(); i++){
                Note n = notes.get(i);
                System.out.printf("| %-3d | %-19s | %-8s | %-33s |\n", i + 1, cut(n.date(), 19), n.type(), cut(n.message(), 33));
            }
            System.out.println("============================================================================");

            int pick = readInt(sc, "Enter # to see details (0 = back): ");
            if(pick == 0) return;
            if(pick < 1 || pick > notes.size()){
                System.out.println(" -> [ERROR] Enter 0-" + notes.size() + ".");
                continue;
            }

            Note n = notes.get(pick - 1);
            System.out.println("\n-------------------- " + n.type() + " --------------------");
            for(String line : n.details()) System.out.println(line);
            System.out.println("-------------------------------------------------");
            System.out.print("\nPress ENTER to go back...");
            sc.nextLine();
        }
    }

    private void myAccount(Customer me){
        String due = me.due_date();
        boolean hasDue = due != null && !due.isBlank();

        String dueStatus = "-";
        if(hasDue){
            try{
                dueStatus = LocalDate.parse(due).isBefore(LocalDate.now()) ? "OVERDUE" : "Current";
            }catch (Exception e){
                dueStatus = "-";
            }
        }
        System.out.println("\n====================================");
        System.out.println("          MY ACCOUNT");
        System.out.println("====================================");
        System.out.printf("\nName            : %s", me.full_name());
        System.out.printf("\nContact Number  : %s", me.contact_number());
        System.out.printf("\nAddress         : %s", me.address());
        System.out.printf("\nUtang Balance   : ₱ %.2f",me.balance());
        System.out.printf("\nCredit Limit    : ₱ %.2f", me.credit_limit());
        System.out.printf("\nAvailable Credit: ₱ %.2f", me.credit_limit() - me.balance());
        System.out.printf("\nDue Date        : %s", hasDue ? due : "-");
        System.out.printf("\nPayment Status  : %s", dueStatus);
        System.out.println("\n====================================");
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

    // ===================== VIEW MY RECORD =====================

    private record Rec(String date, String type, String detail, double amount){}

    private void viewMyRecord(Customer me){
        Map<Integer, String> names = productNames();
        List<Rec> records = new ArrayList<>();

        for(Sales s : salesDao.getAllSales()){
            if(s.customer_id() != me.customer_id()) continue;
            if(!s.payment_type().equalsIgnoreCase("Utang")) continue;

            for(SalesItem it : salesDao.getSaleItems(s.sales_id())){
                String detail = cut(names.getOrDefault(it.product_id(), "Unknown"), 22) + " x" + it.quantity();
                records.add(new Rec(s.Sales_date() == null ? "-" : s.Sales_date(), "UTANG", detail, it.subtotal()));
            }
        }

        for(UtangPayment p : salesDao.getPaymentCus(me.customer_id())){
            records.add(new Rec(p.payment_date() == null ? "-" : p.payment_date(), "PAYMENT", "-", p.amount()));
        }

        records.sort(Comparator.comparing(Rec::date).reversed());

        String filter = null;

        while(true){
            List<Rec> shown = new ArrayList<>();
            double totalUtang = 0, totalPaid = 0;
            for(Rec r : records){
                if(filter != null && !r.type().equals(filter)) continue;
                shown.add(r);
                if(r.type().equals("UTANG")) totalUtang += r.amount();
                else totalPaid += r.amount();
            }

            String title = (filter == null) ? "ALL RECORDS" : filter + " RECORDS";

            System.out.println("\n==============================================================================");
            System.out.println("                          MY RECORD - " + title);
            System.out.printf (" Current Balance: ₱ %.2f\n", me.balance());
            System.out.println("==============================================================================");

            if(shown.isEmpty()){
                System.out.println(" -> No records.");
            }else {
                System.out.printf("| %-19s | %-7s | %-27s | %12s |\n", "Date", "Type", "Product / Qty", "Amount");
                System.out.println("==============================================================================");
                for(Rec r : shown){
                    System.out.printf("| %-19s | %-7s | %-27s | %12.2f |\n",
                            cut(r.date(), 19), r.type(), cut(r.detail(), 27), r.amount());
                }
                System.out.println("==============================================================================");
                if(filter == null){
                    System.out.printf(" Total Utang: ₱ %.2f | Total Paid: ₱ %.2f\n", totalUtang, totalPaid);
                }else if(filter.equals("UTANG")){
                    System.out.printf(" Total Utang: ₱ %.2f\n", totalUtang);
                }else {
                    System.out.printf(" Total Paid: ₱ %.2f\n", totalPaid);
                }
            }

            String choice = readString(sc, "\nFilter ([1] ALL | [2] UTANG | [3] PAYMENT | [0] Back): ").trim();
            switch(choice){
                case "1" -> filter = null;
                case "2" -> filter = "UTANG";
                case "3" -> filter = "PAYMENT";
                case "0" -> { return; }
                default -> System.out.println(" -> [ERROR] Enter 0-3.");
            }
        }
    }
}