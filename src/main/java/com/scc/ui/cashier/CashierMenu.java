package com.scc.ui.cashier;

import com.scc.dao.*;
import com.scc.model.*;
import com.scc.util.InputHelper;
import com.scc.ui.Menu;

import java.util.*;

import static com.scc.util.InputHelper.readString;

public class CashierMenu implements Menu {
    private final UserDao userDao;
    private final ProductDao productDao;
    private final CustomerDao customerDao;
    private final SalesDao salesDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

    public CashierMenu(UserDao userDao, ProductDao productDao, CustomerDao customerDao, SalesDao salesDao, AuditLogDao logDao, Scanner sc) {
        this.userDao = userDao;
        this.productDao = productDao;
        this.customerDao = customerDao;
        this.salesDao = salesDao;
        this.logDao = logDao;
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
                        again = processSale(cashier);
                    } while (again && InputHelper.readYesNo(sc, "\nProcess another sale?"));
                    break;
                case 2:
                    recordUtangPayment(cashier);
                    break;
                case 3:
                    searchCustomer();
                    break;
                case 4:
                    viewProducts();
                    break;
                case 5:
                    System.out.println("\n -> Logging out...");
                    return;
                default:
                    System.out.println("\n -> Invalid choice! Pili ra gikan sa 1 hangtod 5.");
            }
        }
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private Product findProduct(List<Product> list, int id) {
        return list.stream().filter(p -> p.product_id() == id).findFirst().orElse(null);
    }

    private Customer findCustomer(int id) {
        return customerDao.getAllCustomer().stream().filter(c -> c.customer_id() == id).findFirst().orElse(null);
    }

    private String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }

    private double printCart(String title, Map<Integer, Integer> cart, List<Product> products) {
        double total = 0;
        int pad = (78 - title.length()) / 2;

        String border = "+------+--------------------------------+-------+------------+---------------+";
        System.out.println("\n==============================================================================");
        System.out.println(" ".repeat(pad) + title);
        System.out.println("==============================================================================");
        System.out.println(border);
        System.out.printf("| %-4s | %-30s | %5s | %10s | %13s |%n", "ID", "Item", "Qty", "Price", "Subtotal");
        System.out.println(border);

        for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
            Product p = findProduct(products, e.getKey());
            int qty = e.getValue();
            double sub = round2(p.p_price() * qty);
            total += sub;
            System.out.printf("| %-4d | %-30s | %5d | %10.2f | %13.2f |%n",
                    p.product_id(), cut(p.p_name(), 30), qty, p.p_price(), sub);
        }

        total = round2(total);
        System.out.println(border);
        System.out.printf("| %58s | %13.2f |%n", "TOTAL (P)", total);
        System.out.println("+------------------------------------------------------------+---------------+");
        return total;
    }

    private boolean processSale(User cashier) {
        System.out.println("\n===============================");
        System.out.println("          PROCESS SALE");
        System.out.println("===============================");

        List<Product> products = productDao.getProducts().stream()
                .filter(p -> p.p_status().equalsIgnoreCase("AVAILABLE") && p.stock_qty() > 0)
                .toList();

        if (products.isEmpty()) {
            System.out.println("\n -> No available products in stock.");
            return false;
        }

        viewProducts();

        System.out.println("\n-------------- ADD ITEMS --------------");
        Map<Integer, Integer> cart = new LinkedHashMap<>();
        while (true) {
            int pid = InputHelper.readInt(sc, "\nEnter Product ID (0 = done): ");
            if (pid == 0) break;

            Product p = findProduct(products, pid);
            if (p == null) {
                System.out.println(" -> Product not found / unavailable / out of stock.");
                continue;
            }

            int left = p.stock_qty() - cart.getOrDefault(pid, 0);
            if (left <= 0) {
                System.out.println(" -> Wala nay stock nga mahatag para ani.");
                continue;
            }

            int qty = InputHelper.readInt(sc, "Quantity (max " + left + ")     : ");
            if (qty <= 0 || qty > left) {
                System.out.println(" -> Invalid quantity. Pili gikan sa 1 hangtod " + left + ".");
                continue;
            }

            cart.merge(pid, qty, Integer::sum);
            System.out.println(" -> Added: " + p.p_name() + " x" + qty);
        }

        if (cart.isEmpty()) {
            System.out.println("\n -> Sale cancelled (empty cart).");
            return false;
        }

        double total = printCart("CART", cart, products);

        List<SalesItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
            Product p = findProduct(products, e.getKey());
            int qty = e.getValue();
            items.add(new SalesItem(0, 0, p.product_id(), qty, p.p_price(), round2(p.p_price() * qty)));
        }

        System.out.println();
        System.out.println();
        int type = InputHelper.readIntRange(sc, "PAYMENT TYPE ([1] Cash | [2] Utang): ", 1, 2);

        double received = 0;
        Customer customer = null;

        if (type == 1) {
            while (true) {
                received = InputHelper.readDouble(sc, "\nAmount received (0 = cancel): ₱ ");
                if (received == 0) {
                    System.out.println("\n -> Sale cancelled.");
                    return false;
                }
                if (received >= total) break;
                System.out.printf(" -> Kulang pa. Total kay ₱ %.2f%n", total);
            }
            System.out.printf("%nChange : ₱ %.2f%n", round2(received - total));
        } else {
            int cid = InputHelper.readInt(sc, "\nCustomer ID (0 to cancel): ");
            if (cid == 0) {
                System.out.println("\n -> Sale cancelled.");
                return false;
            }
            customer = findCustomer(cid);
            if (customer == null) {
                System.out.println("\n -> Customer not found.");
                return false;
            }
            if (!customer.status().equalsIgnoreCase("ACTIVE")) {
                System.out.println("\n -> Customer is " + customer.status() + ". Dili pwede mag-utang.");
                return false;
            }
            double available = customer.credit_limit() - customer.balance();
            System.out.println();
            System.out.printf("Customer         : %s%n", customer.full_name());
            System.out.printf("Available credit : ₱ %.2f%n", available);
            if (total > available) {
                System.out.println("\n -> Credit limit exceeded. Dili ma-process ang utang.");
                return false;
            }
        }

        System.out.println();
        if (!InputHelper.readYesNo(sc, "Confirm sale?")) {
            System.out.println("\n -> Sale cancelled.");
            return false;
        }

        if (customer != null) {
            System.out.println("\n-------------- PIN CONFIRMATION --------------");
            System.out.println("Enter [0] to Cancel");
            while (true) {
                String pin = InputHelper.readPin(sc, "\nCustomer PIN: ");
                if (pin.equals("0")) {
                    System.out.println("\n -> Sale Cancelled.");
                    return true;
                }

                if (customerDao.verifyPin(customer.customer_id(), pin)) {
                    break;
                }

                logDao.logs(cashier.user_id(), "UTANG PIN FAILED", "customer_tbl", customer.customer_id(),
                        "Wrong PIN on utang sale for customer " + customer.customer_id());

                Customer fresh = findCustomer(customer.customer_id());
                if (fresh == null || !fresh.status().equalsIgnoreCase("ACTIVE")) {
                    System.out.println("\n -> Sale Cancelled. Na-lock ang PIN sa customer.");
                    return true;
                }
            }
        }

        Integer customerId = (customer == null) ? null : customer.customer_id();
        int saleId = salesDao.processSale(cashier.user_id(), customerId,
                type == 1 ? "Cash" : "Utang", items, received);

        if (saleId <= 0) {
            System.out.println("\n -> Sale failed. Walay na-save.");
            return false;
        }

        logDao.logs(cashier.user_id(), "SALE", "sales", saleId,
                String.format("%s sale, total %.2f", type == 1 ? "Cash" : "Utang", total));

        System.out.println("\n=========================== RECEIPT ===========================");
        System.out.printf("Sale #      : %d%n", saleId);
        System.out.printf("Payment     : %s%n", type == 1 ? "Cash" : "Utang");
        printCart("ITEMS PURCHASED", cart, products);
        System.out.println();

        if (type == 1) {
            System.out.printf("Received    : ₱ %.2f%n", received);
            System.out.printf("Change      : ₱ %.2f%n", round2(received - total));
        } else {
            Customer updated = findCustomer(customer.customer_id());
            String due = (updated.due_date() == null || updated.due_date().isBlank()) ? "-" : updated.due_date();
            System.out.printf("Customer    : %s%n", updated.full_name());
            System.out.printf("New balance : ₱ %.2f%n", updated.balance());
            System.out.printf("Due date    : %s%n", due);
        }
        System.out.println("===============================================================");
        return true;
    }

    private void recordUtangPayment(User cashier) {
        System.out.println("\n===============================");
        System.out.println("      RECORD UTANG PAYMENT");
        System.out.println("===============================");

        int cid = InputHelper.readInt(sc, "\nCustomer ID (0 to cancel): ");
        if (cid == 0) {
            System.out.println("\n -> Operation Cancelled.");
            return;
        }

        Customer customer = findCustomer(cid);
        if (customer == null) {
            System.out.println("\n -> Customer not found.");
            return;
        }

        double balance = round2(customer.balance());
        if (balance <= 0) {
            System.out.println("\n -> Walay utang ni " + customer.full_name() + ".");
            return;
        }

        String due = (customer.due_date() == null || customer.due_date().isBlank()) ? "-" : customer.due_date();
        System.out.println();
        System.out.printf("Customer        : %s%n", customer.full_name());
        System.out.printf("Current balance : ₱ %.2f%n", balance);
        System.out.printf("Due date        : %s%n", due);

        double amount;
        while (true) {
            amount = InputHelper.readDouble(sc, "\nPayment amount (0 = cancel): ₱ ");
            if (amount == 0) {
                System.out.println("\n -> Operation Cancelled.");
                return;
            }
            if (amount < 0) {
                System.out.println(" -> Invalid amount.");
                continue;
            }
            if (round2(amount) > balance) {
                System.out.printf(" -> Sobra sa utang. Balance kay ₱ %.2f%n", balance);
                continue;
            }
            break;
        }
        amount = round2(amount);

        System.out.println();
        if (!InputHelper.readYesNo(sc, "Confirm payment of ₱ " + String.format("%.2f", amount) + "?")) {
            System.out.println("\n -> Payment cancelled.");
            return;
        }

        int paymentId = salesDao.recordUtangPayment(customer.customer_id(), amount, cashier.user_id());
        if (paymentId <= 0) {
            System.out.println("\n -> Payment failed. Walay na-save.");
            return;
        }

        logDao.logs(cashier.user_id(), "UTANG PAYMENT", "utang_payments", paymentId,
                String.format("Payment %.2f from customer %d", amount, customer.customer_id()));

        Customer updated = findCustomer(customer.customer_id());
        String newDue = (updated.due_date() == null || updated.due_date().isBlank()) ? "-" : updated.due_date();

        System.out.println("\n======================= PAYMENT RECEIPT =======================");
        System.out.printf("Payment #       : %d%n", paymentId);
        System.out.printf("Customer        : %s%n", updated.full_name());
        System.out.printf("Previous balance: ₱ %.2f%n", balance);
        System.out.printf("Amount paid     : ₱ %.2f%n", amount);
        System.out.printf("New balance     : ₱ %.2f%n", updated.balance());
        System.out.printf("Due date        : %s%n", newDue);
        System.out.println("===============================================================");
    }

    private void searchCustomer() {
        System.out.println("\n===============================");
        System.out.println("        SEARCH CUSTOMER");
        System.out.println("===============================");

        String keyword = readString(sc, "\nEnter name or ID (0 to cancel): ");
        if (keyword.equals("0")) {
            System.out.println("\n -> Operation Cancelled.");
            return;
        }

        List<Customer> matches = customerDao.getAllCustomer().stream()
                .filter(c -> c.full_name().toLowerCase().contains(keyword.toLowerCase())
                        || String.valueOf(c.customer_id()).equals(keyword))
                .toList();

        if (matches.isEmpty()) {
            System.out.println("\n -> No customer found.");
            return;
        }

        for (Customer c : matches) {
            double available = c.credit_limit() - c.balance();
            String due = (c.due_date() == null || c.due_date().isBlank()) ? "-" : c.due_date();

            System.out.println("\n-------------------------------");
            System.out.println("Customer ID  : " + c.customer_id());
            System.out.println("Name         : " + c.full_name());
            System.out.println("Contact      : " + c.contact_number());
            System.out.printf("Credit Limit : ₱ %.2f%n", c.credit_limit());
            System.out.printf("Balance      : ₱ %.2f%n", c.balance());
            System.out.printf("Available    : ₱ %.2f%n", available);
            System.out.println("Due Date     : " + due);
            System.out.println("Agreement    : " + c.agreement_on_file());
            System.out.println("Status       : " + c.status());
            System.out.println("-------------------------------");
        }
    }

    private void viewProducts() {
        List<Product> products = productDao.getProducts().stream()
                .filter(p -> p.p_status().equalsIgnoreCase("AVAILABLE"))
                .toList();

        System.out.println("\n==============================================================================");
        System.out.println("                                PRODUCT LIST");
        System.out.println("==============================================================================");
        if (products.isEmpty()) {
            System.out.println("\n -> No Records.");
            return;
        }
        System.out.println("+------+--------------------------------+----------------+----------+--------+");
        System.out.printf("| %-4s | %-30s | %-14s | %8s | %6s |%n", "ID", "Name", "Category", "Price", "Stock");
        System.out.println("+------+--------------------------------+----------------+----------+--------+");
        for (Product p : products) {
            System.out.printf("| %-4d | %-30s | %-14s | %8.2f | %6d |%n",
                    p.product_id(), cut(p.p_name(), 30), cut(p.p_category(), 14), p.p_price(), p.stock_qty());
        }
        System.out.println("+------+--------------------------------+----------------+----------+--------+");
    }
}