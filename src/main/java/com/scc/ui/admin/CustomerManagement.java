package com.scc.ui.admin;

import com.scc.dao.AuditLogDao;
import com.scc.dao.CustomerDao;
import com.scc.dao.UserDao;
import com.scc.model.Customer;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

import static com.scc.util.InputHelper.*;

public class CustomerManagement {
    private final CustomerDao customerDao;
    private final UserDao userDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

    public CustomerManagement(UserDao userDao, CustomerDao customerDao, AuditLogDao logDao, Scanner sc){
        this.customerDao = customerDao;
        this.userDao = userDao;
        this.logDao = logDao;
        this.sc = sc;
    }

    public void show(User admin){
        while (true){
            System.out.println("\n===============================");
            System.out.println("       MANAGE CUSTOMERS");
            System.out.println("===============================");
            System.out.println("[1] Register Customer\n[2] View Customers\n[3] Edit Customer\n[4] Reset PIN" +
                    "\n[5] Deactivate / Activate Customer\n[6] Delete Customer\n[0] Back");
            String choice = readString(sc, "Enter Choice: ");

            switch (choice){
                case "1" -> addCustomer(admin);
                case "2" -> viewCustomer();
                case "3" -> editCustomer(admin);
                case "4" -> resetPin(admin);
                case "5" -> updateStatus(admin);
                case "6" -> deleteCustomer(admin);
                case "0" -> { return; }
                default -> System.out.println("Invalid choice! 0 to 6 only.");
            }
        }
    }

    private void addCustomer(User admin){
        System.out.println("\n===============================");
        System.out.println("       REGISTER CUSTOMER");
        System.out.println("===============================");

        String fullName = readString(sc, "Enter Customer full name (0 to cancel): ");
        if(fullName.equals("0")){
            System.out.println("\n -> Operation Cancelled.");
            return;
        }

        if(!readYesNo(sc, "Signed utang agreement on file?")){
            System.out.println(" -> [ERROR] A signed agreement is required before registering a customer.");
            return;
        }
        String agreement = "Yes";

        String contact = readString(sc, "Enter Contact number: ");
        String address = readString(sc, "Enter address: ");

        double creditLimit;
        while(true){
            creditLimit = readDouble(sc, "Enter Credit Limit: ");
            if(creditLimit >= 0) break;
            System.out.println(" -> [ERROR] Credit limit cannot be negative.");
        }

        System.out.println("\n-- Customer Login Account --");
        String username;
        while (true){
            System.out.print("Enter username: ");
            username = sc.nextLine().trim().toLowerCase();

            if(username.length() < 5){
                System.out.println(" -> [ERROR] Username must be at least 5 characters.");
            }else if(!username.matches("^[a-z0-9_]+$")){
                System.out.println(" -> [ERROR] Use letters, numbers, and underscore only (no spaces).");
            }else if(userDao.usernameExists(username)){
                System.out.println(" -> [ERROR] Username already taken.");
            }else {
                break;
            }
        }

        String password;
        while (true){
            System.out.print("Enter Password: ");
            password = sc.nextLine().trim();
            if(password.length() < 5){
                System.out.println(" -> [ERROR] Password must be at least 5 characters.");
                continue;
            }
            System.out.print("Confirm Password: ");
            if(sc.nextLine().trim().equals(password)) break;
            System.out.println(" -> [ERROR] Passwords do not match.");
        }

        String pin;
        while (true){
            pin = readPin(sc, "Set 4-digit PIN: ");
            if(readPin(sc, "Confirm PIN: ").equals(pin)) break;
            System.out.println(" -> [ERROR] PINs do not match.");
        }

        System.out.println("\n-------------------------------");
        System.out.println("Name         : " + fullName);
        System.out.println("Contact      : " + contact);
        System.out.println("Address      : " + address);
        System.out.printf ("Credit Limit : ₱ %.2f\n", creditLimit);
        System.out.println("Agreement    : " + agreement);
        System.out.println("Username     : " + username);
        System.out.println("-------------------------------");

        if(readYesNo(sc, "Register this Customer?")){
            int id = customerDao.registerCustomer(fullName, username, password, contact, address, creditLimit, agreement, pin);
            if(id != -1){
                logDao.logs(admin.user_id(), "ADD CUSTOMER", "customer_tbl", id, "Added Customer: " + fullName +
                        " | Username: " + username + " | Credit Limit: " + creditLimit + " | Agreement: " + agreement);
            }
        }else {
            System.out.println("\n -> Operation Cancelled");
        }
    }

    private  void displayCustomerTable(List<Customer> customerList, String title){
        System.out.println("\n=====================================================================================================");
        System.out.println("                                         " + title + " ");
        if(customerList.isEmpty()){
            System.out.println("\n-> No Records.");
            System.out.println("-----------------------------------------------------------------------------------------------------");
            return;
        }
        System.out.println("+------+--------------------------+----------------+--------------+--------------+-----------+----------+");
        System.out.printf ("| %-4s | %-24s | %-14s | %12s | %12s | %-9s | %-8s |\n", "ID", "Name", "Contact", "Credit Limit", "Balance", "Agreement", "Status");
        System.out.println("+------+--------------------------+----------------+--------------+--------------+-----------+----------+");
        for(Customer c : customerList){
            System.out.printf("| %-4d | %-24s | %-14s | %12.2f | %12.2f | %-9s | %-8s |\n", c.customer_id(), c.full_name(), c.contact_number(), c.credit_limit(),
                    c.balance(), c.agreement_on_file(),  c.status());
        }
        System.out.println("+------+--------------------------+----------------+--------------+--------------+-----------+----------+");
    }

    private List<Customer> filterCustomer(String status, String keyword){
        return customerDao.getAllCustomer().stream().filter(c -> status == null || c.status().equalsIgnoreCase(status))
                .filter(c -> keyword == null || c.full_name().toLowerCase().contains(keyword.toLowerCase())
                        || String.valueOf(c.customer_id()).equals(keyword)).toList();
    }

    private void viewCustomer(){
        String status = null;
        String keyword = null;

        while(true){
            String title = (status == null) ? "ALL CUSTOMERS" : status + " CUSTOMERS";
            if(keyword != null) title += " (search: " + keyword + ")";
            displayCustomerTable(filterCustomer(status, keyword), title);

            String choice = readString(sc, "Filter ([1] ALL | [2] ACTIVE | [3] INACTIVE | [4] LOCKED | [5] Search | [0] Back): ");

            switch (choice){
                case "1" -> {status = null; keyword = null;}
                case "2" -> status = "ACTIVE";
                case "3" -> status = "INACTIVE";
                case "4" -> status = "LOCKED";
                case "5" -> keyword = readString(sc, "Enter name or ID to search: ");
                case "0" -> {return;}
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 5 only.");
            }
        }
    }

    private Customer selectCustomer(String status, String title){
        List<Customer> customerList = filterCustomer(status, null);
        displayCustomerTable(customerList, title);

        if(customerList.isEmpty()) return null;

        int id = readInt(sc, "Enter customer ID (0 to cancel): ");
        if(id == 0){
            System.out.println(" -> Operation Cancelled.");
            return null;
        }

        Customer target = customerList.stream().filter(c -> c.customer_id() == id).findFirst().orElse(null);

        if(target == null){
            System.out.println(" -> [ERROR] Customer ID not found.");
            return null;
        }
        return target;
    }

    private void editCustomer(User admin){
        Customer target = selectCustomer(null, "EDIT CUSTOMER");
        if(target == null) return;

        System.out.println("\n--------------------------------------------");
        System.out.println("Click [Enter] to Keep");

        System.out.print("\nEnter name [ " + target.full_name() + " ]: ");
        String fullName = sc.nextLine().trim();
        if(fullName.isEmpty()) fullName = target.full_name();

        System.out.print("Enter Contact [ " + target.contact_number() + " ]: ");
        String contact = sc.nextLine().trim();
        if(contact.isEmpty()) contact = target.contact_number();

        System.out.print("Enter Address [ " + target.address() + " ]: ");
        String address = sc.nextLine().trim();
        if(address.isEmpty()) address = target.address();

        double creditLimit = target.credit_limit();
        while (true){
            System.out.print("Enter Credit Limit [ " + target.credit_limit() + " ]: ");
            String in = sc.nextLine().trim();

            if(in.isEmpty()) break;
            try{
                double newLimit = Double.parseDouble(in);
                if(newLimit < 0){
                    System.out.println(" -> [ERROR] Credit limit cannot be negative.");
                }else if(newLimit < target.balance()){
                    System.out.println(" -> [ERROR] Credit limit cannot be lower than the current balance (" + target.balance() + ").");
                }else {
                    creditLimit = newLimit;
                    break;
                }
            }catch (NumberFormatException e){
                System.out.println(" -> [ERROR] Invalid number.");
            }
        }

        String agreement = target.agreement_on_file();
        while (true){
            System.out.print("Agreement on file [ " + target.agreement_on_file() + " ] (Y/N): ");
            String in = sc.nextLine().trim().toLowerCase();
            if(in.isEmpty()) break;
            if(in.equals("y") || in.equals("yes")){ agreement = "Yes"; break; }
            if(in.equals("n") || in.equals("no")){ agreement = "No"; break; }
            System.out.println(" -> [ERROR] Enter Y or N.");
        }

        if(readYesNo(sc, "Save Changes? ")){
            if(customerDao.updateCustomers(target.customer_id(), fullName, contact, address, creditLimit, agreement)){
                logDao.logs(admin.user_id(), "UPDATE CUSTOMER", "customer_tbl", target.customer_id(), "Updated customer: " + fullName +
                        " | Contact: " + contact + " | Credit Limit: " + creditLimit + " | Agreement: " + agreement);
            }
        }else {
            System.out.println(" -> Update Cancelled.");
        }
    }

    private void resetPin(User admin){
        Customer target = selectCustomer(null, "RESET CUSTOMER PIN");
        if(target == null) return;

        String pin;
        while (true){
            pin = readPin(sc, "Enter new 4-digit PIN: ");
            if(readPin(sc, "Confirm new PIN: ").equals(pin)) break;
            System.out.println(" -> [ERROR] PINs do not match.");
        }

        if(readYesNo(sc, "Reset PIN of " + target.full_name() + "?")){
            if(customerDao.updatePin(target.customer_id(), pin)){
                String note = target.status().equals("LOCKED") ? " (customer unlocked)" : "";
                logDao.logs(admin.user_id(), "RESET CUSTOMER PIN", "customer_tbl", target.customer_id(),
                        "Reset PIN of customer: " + target.full_name() + note);
            }
        }else {
            System.out.println(" -> Cancelled.");
        }
    }

    private void updateStatus(User admin){
        Customer target = selectCustomer(null, "Customer (ACTIVE/ INACTIVE)");
        if(target == null) return;

        if(target.status().equalsIgnoreCase("LOCKED")){
            System.out.println(" -> [ERROR] Customer is LOCKED. Use Reset PIN to unlock first.");
            return;
        }

        String newStatus = target.status().equalsIgnoreCase("ACTIVE") ? "INACTIVE" : "ACTIVE";

        if(readYesNo(sc, "Change " + target.full_name() + " to " + newStatus + "?")){
            if(customerDao.updateStatus(target.customer_id(), newStatus)){
                logDao.logs(admin.user_id(), "UPDATE CUSTOMER STATUS", "customer_tbl", target.customer_id(),
                        "Changed customer: " + target.full_name() + " status from: " + target.status() + " to: " + newStatus);
            }
        }else {
            System.out.println(" -> Cancelled.");
        }
    }

    private void deleteCustomer(User admin){
        Customer target = selectCustomer(null, "CUSTOMER DELETION");
        if(target == null) return;

        if(target.balance() > 0){
            System.out.println(" -> [ERROR] Cannot delete: customer still has a balance of " + target.balance() + ".");
            return;
        }

        System.out.println("-------------------------------");
        System.out.println("Name    : " + target.full_name());
        System.out.println("Contact : " + target.contact_number());
        System.out.println("Limit   : " + target.credit_limit());
        System.out.println("Status  : " + target.status());
        System.out.println("-------------------------------");
        System.out.println("Note: mapapas pud ang login account sa customer.");

        if(readYesNo(sc, "Delete this Customer?")){
            if(customerDao.deleteCustomer(target.customer_id())){
                logDao.logs(admin.user_id(), "DELETE CUSTOMER", "customer_tbl", target.customer_id(),
                        "Deleted customer: " + target.full_name() + " | Contact: " + target.contact_number());
            }
        }else {
            System.out.println("\n -> Cancelled.");
        }
    }
}