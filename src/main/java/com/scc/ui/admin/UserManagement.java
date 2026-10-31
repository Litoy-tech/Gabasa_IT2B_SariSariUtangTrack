package com.scc.ui.admin;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.InputHelper;


import java.util.List;
import java.util.Scanner;

import static com.scc.ui.InputHelper.*;

public class UserManagement {
    private final UserDao userDao;
    private final Scanner sc;

    public UserManagement(UserDao userDao, Scanner sc){
        this.userDao = userDao;
        this.sc = sc;
    }

    public void show(User admin) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("         MANAGE USERS");
            System.out.println("===============================");
            System.out.println("[1] Add User\n[2] View All Users\n[3] Approved User\n[4] Change Role" +
                    "\n[5] Change Status (Approved/ Inactive)\n[6] Delete User\n[0] Back");
            String choice = InputHelper.readString(sc, "Enter Choice: ");

            switch (choice) {
                case "1" -> registerUser();
                case "2" -> viewUsers();
                case "3" -> approvedUser();
                case "4" -> updateUserRole(admin);
                case "5" -> updateUserStatus(admin);
                case "6" -> deleteUser(admin);
                case "0" -> {return;}
                default -> System.out.println("Invalid choice! 0 to 6 only.");
            }
        }
    }

    private void displayUserTable(List<User> users, String title){
        System.out.println("\n==================================================================================================");
        System.out.println("                                         " +title+ " " );
        if(users.isEmpty()){
            System.out.println("\n-> No Records.");
            System.out.println("--------------------------------------------------------------------------------------------------");
            return;
        }

        System.out.println("+------+--------------------+--------------------+--------------+----------+---------------------+");
        System.out.printf("| %-4s | %-18s | %-18s | %-12s | %-8s | %-19s |\n", "ID", "Fullname", "Username", "Role", "Status", "Date Created");
        System.out.println("+------+--------------------+--------------------+--------------+----------+---------------------|");
            for(User u: users){
                System.out.printf("| %-4s | %-18s | %-18s | %-12s | %-8s | %-15s |\n", u.user_id(), u.fullName(), u.username(), u.roles(), u.status(), u.created_at());
            }
        System.out.println("+------+--------------------+--------------------+--------------+----------+---------------------+");
    }

    private List<User> filterByStatus(String status) {
        List<User> all = userDao.getAllUser();
        if (status == null) return all;
        return all.stream()
                .filter(u -> u.status().equalsIgnoreCase(status))
                .toList();
    }


    private void registerUser() {

        System.out.println("\n    ===== REGISTER USER =====");

        String full_name;
        while (true) {
            System.out.print("Enter your Fullname: ");
            full_name = sc.nextLine().trim();

            if (full_name.isEmpty()) {
                System.out.println("Invalid! Fullname cannot be empty.");
            } else {
                break;
            }
        }

        String user;
        while (true) {
            System.out.print("Enter username: ");
            user = sc.nextLine().trim().toLowerCase();

            if (user.length() < 5) {
                System.out.println("Invalid! Username must be at least 5 characters.");
            } else if (!user.matches("^[a-z0-9_]+$")) {
                System.out.println("Invalid! Use letters, numbers, and underscore only (no spaces).");
            } else if (userDao.usernameExists(user)) {
                System.out.println("Invalid! Username already taken.");
            } else {
                break;
            }
        }

        String pass;
        while (true) {
            System.out.print("Enter Password: ");
            pass = sc.nextLine().trim();

            if (pass.length() < 5) {
                System.out.println("Invalid! Password must be at least 5 characters.");
                continue;
            }

            boolean confirmed = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                System.out.print("Confirm Password: ");
                String confirm = sc.nextLine().trim();

                if (confirm.equals(pass)) {
                    confirmed = true;
                    break;
                }
                System.out.println("\nPasswords do not match! Attempt " + attempt + " of 3.");
            }

            if (confirmed) {
                break;
            }
            System.out.println("Too many wrong attempts. Register Again.");
            return;
        }

        String role;
        while (true) {
            System.out.println("\n-- SELECT ROLE --");
            System.out.println(" [1] OWNER (admin)");
            System.out.println(" [2] CASHIER");
            System.out.println(" [3] SYSTEM_AUDITOR");
            System.out.print(" Enter Choice: ");
            String roleChoice = sc.nextLine().trim();

            if (roleChoice.equals("1")) {
                role = "OWNER";
                break;
            } else if (roleChoice.equals("2")) {
                role = "CASHIER";
                break;
            } else if (roleChoice.equals("3")) {
                role = "SYS_AUDITOR";
                break;
            } else {
                System.out.println("Invalid! Please select 1, 2, or 3 only.");
            }
        }

        String status;
        while (true){
            status = InputHelper.readString(sc, "\n-- SELECT STATUS --\n [1] APPROVED\n [2] PENDING\n Enter Choice: ");
            switch (status){
                case "1" -> status = "APPROVED";
                case "2" -> status = "PENDING";
                default -> System.out.println(" Invalid Input! Enter 1 or 2.");
            }
            break;
        }

        if(readYesNo(sc, "Add this user?")){
            userDao.addUser(full_name, user, pass, role, status);
        }else {
            System.out.println(" -> Operation Cancelled.");
        }

    }

    private void viewUsers() {
        String status = null;

        while (true) {
            String title = (status == null) ? "ALL REGISTERED USERS" : status + " USERS";
            displayUserTable(filterByStatus(status), title);

            String choice = readString(sc,
                    "Filter ([1] ALL | [2] APPROVED | [3] PENDING | [4] INACTIVE | [0] Back): ").trim();

            switch (choice) {
                case "1" -> status = null;
                case "2" -> status = "APPROVED";
                case "3" -> status = "PENDING";
                case "4" -> status = "INACTIVE";
                case "0" -> { return; }
                default  -> System.out.println(" -> [ERROR] Invalid choice. 0 to 4 only.");
            }
        }
    }

    private void approvedUser(){
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, " PENDING APPROVALS ");

        if(pendingUsers.isEmpty()){return;}

        int userId = readInt(sc, "Enter User ID to approved (0 to Cancel): ");

        if(userId == 0){
            System.out.println(" -> Operation Cancelled by the user.");
            return;
        }

        boolean exits = pendingUsers.stream().anyMatch(u -> u.user_id() == userId);
        if(!exits){
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        if(readYesNo(sc, "Approved this User?")){
            userDao.approvedUser(userId);
        }else{
            System.out.println(" -> Cancelled.");
        }
    }

    private void updateUserRole(User admin){
        List<User> users = userDao.getAllUser();
        displayUserTable(users, "CHANGE ROLE");

        if(users.isEmpty()) return;

        int userId = readInt(sc, "Enter User ID (0 to cancel): ");
        if(userId == 0){
            System.out.println(" -> Operation Cancelled");
            return;
        }

        if(userId == admin.user_id()){
            System.out.println(" -> [ERROR] You cannot change your own role.");
            return;
        }

        User target = users.stream().filter(u -> u.user_id() == userId).findFirst().orElse(null);

        if(target == null){
            System.out.println(" -> [ERROR] User ID not found.");
            return;
        }

        String newRole = readValidRole();
        if(newRole == null){
            System.out.println(" -> Operation Cancelled");
            return;
        }

        if(newRole.equalsIgnoreCase(target.roles())){
            System.out.println(" -> [ERROR] User is already " + newRole + ".");
            return;
        }

        if(readYesNo(sc, " Change " + target.username() + " from " + target.roles() + " to " + newRole + "?")){
            userDao.updateUserRoleAndStatus(userId, newRole, target.status());
        }else{
            System.out.println(" -> Cancelled.");
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("\n Select New Role ( [1] ADMIN | [2] CASHIER | [3] SYSTEM_AUDITOR | [0] Cancel )");
            String choice = readString(sc, " Enter Choice: ").trim();

            switch (choice) {
                case "1": return "ADMIN";
                case "2": return "CASHIER";
                case "3": return "SYS_AUDITOR";
                case "0": return null;
                default:  System.out.println(" -> [ERROR] Invalid role. Choose 0 to 3 only.");
            }
        }
    }

    private void updateUserStatus(User admin){
        List<User> users = userDao.getAllUser();
        displayUserTable(users, "CHANGE STATUS");

        if(users.isEmpty()) return;

        int userId = readInt(sc, "Enter User ID (0 to cancel): ");
        if(userId == 0){
            System.out.println(" -> Operation Cancelled");
            return;
        }

        if(userId == admin.user_id()){
            System.out.println(" -> [ERROR] You cannot change your own status.");
            return;
        }

        User target = users.stream().filter(u -> u.user_id() == userId).findFirst().orElse(null);

        if(target == null){
            System.out.println(" -> [ERROR] User ID not found.");
            return;
        }

        String newStatus = readValidStatus();
        if(newStatus == null){
            System.out.println(" -> Operation Cancelled");
            return;
        }

        if(newStatus.equalsIgnoreCase(target.status())){
            System.out.println(" -> [ERROR] User is already " + newStatus + ".");
            return;
        }

        if(readYesNo(sc, " Change " + target.username() + " from " + target.status() + " to " + newStatus + "?")){
            userDao.updateUserRoleAndStatus(userId, target.roles(), newStatus);
        }else{
            System.out.println(" -> Cancelled.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println("\n Select New Status ( [1] APPROVED | [2] PENDING | [3] INACTIVE | [0] Cancel )");
            String choice = readString(sc, " Enter Choice: ").trim();

            switch (choice) {
                case "1": return "APPROVED";
                case "2": return "PENDING";
                case "3": return "INACTIVE";
                case "0": return null;
                default:  System.out.println(" -> [ERROR] Invalid status. Choose 0 to 3 only.");
            }
        }
    }

    private void deleteUser(User admin){

        List<User> deletingUsers = userDao.getAllUser().stream().filter(u -> !u.status().equalsIgnoreCase("APPROVED")).toList();
        displayUserTable(deletingUsers, "DELETE USER");

        if(deletingUsers.isEmpty()){
            return;
        }

        int userId = readInt(sc, "Enter User ID to delete (0 to cancel): ");

        if(userId == 0){
            System.out.println(" -> Operation Cancelled by the user.");
            return;
        }

        if(userId == admin.user_id()){
            System.out.println(" -> [ERROR] You cannot delete your own account.");
            return;
        }

        boolean exist = deletingUsers.stream().anyMatch(u -> u.user_id() == userId);

        if(!exist){
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        if(readYesNo(sc, "Delete this User?")){
            userDao.deletedUser(userId);
        }else{
            System.out.println(" -> Cancelled.");
        }
    }

}