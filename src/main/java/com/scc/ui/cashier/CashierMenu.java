package com.scc.ui.cashier;

import com.scc.dao.CustomerDao;
import com.scc.dao.ProductDao;
import com.scc.dao.UserDao;
import com.scc.model.Customer;
import com.scc.model.Product;
import com.scc.model.User;
import com.scc.util.InputHelper;
import com.scc.ui.Menu;

import java.util.List;
import java.util.Scanner;

import static com.scc.util.InputHelper.readString;

public class CashierMenu implements Menu {
    private final UserDao userDao;
    private final ProductDao productDao;
    private final CustomerDao customerDao;
    private final Scanner sc;

    public CashierMenu(UserDao userDao, ProductDao productDao, CustomerDao customerDao, Scanner sc) {
        this.userDao = userDao;
        this.productDao = productDao;
        this.customerDao = customerDao;
        this.sc = sc;
    }

    @Override
    public void show(User cashier) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("          CASHIER MENU");
            System.out.println("   Welcome, " + cashier.fullName() + "!");
            System.out.println("===============================");
            System.out.println("[1] Process Sale\n[2] Record Utang Payment\n[3] Search Customer\n[4] View Product List\n[5] Logout");

            int choice = InputHelper.readInt(sc, "Enter Choice: ");

            switch (choice) {
                case 1:
                    System.out.println("Process Sale (soon)");
                    break;
                case 2:
                    System.out.println("Record Utang Payment (soon)");
                    break;
                case 3:
                    searchCustomer();
                    break;
                case 4:
                    viewProducts();
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice! Pili ra gikan sa 1 hangtod 5.");
            }
        }
    }


    private void searchCustomer(){
        System.out.println("\n===============================");
        System.out.println("        SEARCH CUSTOMER");
        System.out.println("===============================");

        String keyword = readString(sc, "Enter name or ID (0 to cancel): ");
        if (keyword.equals("0")) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        List<Customer> matches = customerDao.getAllCustomer().stream()
                .filter(c -> c.full_name().toLowerCase().contains(keyword.toLowerCase())
                        || String.valueOf(c.customer_id()).equals(keyword))
                .toList();

        if (matches.isEmpty()) {
            System.out.println(" -> No customer found.");
            return;
        }

        for (Customer c : matches) {
            double available = c.credit_limit() - c.balance();
            String due = (c.due_date() == null || c.due_date().isBlank()) ? "-" : c.due_date();

            System.out.println("\n-------------------------------");
            System.out.println("Customer ID  : " + c.customer_id());
            System.out.println("Name         : " + c.full_name());
            System.out.println("Contact      : " + c.contact_number());
            System.out.printf ("Credit Limit : ₱ %.2f\n", c.credit_limit());
            System.out.printf ("Balance      : ₱ %.2f\n", c.balance());
            System.out.printf ("Available    : ₱ %.2f\n", available);
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
        System.out.println("                             PRODUCT LIST");
        if (products.isEmpty()) {
            System.out.println("\n-> No Records.");
            System.out.println("------------------------------------------------------------------------");
            return;
        }
        System.out.println("+------+--------------------------------+----------------+----------+--------+");
        System.out.printf ("| %-4s | %-30s | %-14s | %8s | %6s |\n", "ID", "Name", "Category", "Price", "Stock");
        System.out.println("+------+--------------------------------+----------------+----------+--------+");
        for (Product p : products) {
            System.out.printf("| %-4d | %-30s | %-14s | %8.2f | %6d |\n",
                    p.product_id(), p.p_name(), p.p_category(), p.p_price(), p.stock_qty());
        }
        System.out.println("+------+--------------------------------+----------------+----------+--------+");
    }
}