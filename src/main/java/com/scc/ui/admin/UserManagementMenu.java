package com.scc.ui.admin;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.InputHelper;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.scc.ui.InputHelper.*;

public class UserManagementMenu {
    private final UserDao userDao;
    private final Scanner sc;

    public UserManagementMenu(UserDao userDao, Scanner sc){
        this.userDao = userDao;
        this.sc = sc;
    }

    public void show() {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("         MANAGE USERS");
            System.out.println("===============================");
            System.out.println("[1] Add User\n[2] View All Users\n[3] View Pending User\n[4] Approved User\n[5] Update User(Roles/ Status)" +
                    "\n[6] Deactivate User\n[0] Back");
            String choice = InputHelper.readString(sc, "Enter Choice: ");

            switch (choice) {
                case "1" -> System.out.println("Add User (soon)");
                case "2" -> viewAllUser();
                case "3" -> viewPendingUser();
                case "4" -> approvedUser();
                case "5" -> System.out.println("Padulong pacl");
                case "6" -> deleteUser();
                case "0" -> {return;}
                default -> System.out.println("Invalid choice! 0 to 6 only.");
            }
        }
    }

    private void displayUserTable(List<User> users, String title){
        System.out.println("\n====================================== " +title+ " ======================================");

        if(users.isEmpty()){
            System.out.println("\n-> No Records.");
            return;
        }

        System.out.println("\n+------+--------------------+--------------------+--------------+----------+---------------------+");
        System.out.printf("| %-4s | %-18s | %-18s | %-12s | %-8s | %-19s |\n", "ID", "Fullname", "Username", "Role", "Status", "Date Created");
        System.out.println("+------+--------------------+--------------------+--------------+----------+---------------------|");
            for(User u: users){
                System.out.printf("| %-4s | %-18s | %-18s | %-12s | %-8s | %-15s |\n", u.user_id(), u.fullName(), u.username(), u.roles(), u.status(), u.created_at());
            }
        System.out.println("+------+--------------------+--------------------+--------------+----------+---------------------+");


    }

    private void viewAllUser(){
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USER");
    }

    private void viewPendingUser(){
        displayUserTable(userDao.getPendingUser(), " PENDING APPROVALS ");
    }

    private void approvedUser(){
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, " PENDING APPROVALS ");

        if(pendingUsers.isEmpty()){
            return;
        }

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

    private void UpdateUser(User currentAdmin){
        List<User> users = userDao.getAllUser();
        displayUserTable(users, " UPDATING USER ");

        if(users.isEmpty()){
            return;
        }

        int userId = readInt(sc, "Enter ID to update user (0 to cancel): ");

        if(userId == 0){
            System.out.println(" -> Operation Cancelled by the user.");
            return;
        }

        if(userId == currentAdmin.user_id()){
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted.");
        }

        System.out.println(" [1] ADMIN\n [2] CASHIER\n [3] SYSTEM_AUDITOR");
        String newRole = readString(sc, "Enter New Role (0 to cancel): ");

        switch (newRole){
            case "1":
                newRole = "ADMIN";
                break;
            case "2":
                newRole = "CASHIER";
                break;
            case "3":
                newRole = "SYSTEM AUDITOR";
                break;
            case "0":
                return;
            default:
                System.out.println("Please Enter 0 - 3 only.");
        }

        int newStatus = readInt(sc, "Enter New Status (0 to cancel): ");

    }

    private void deleteUser(){
        List<User> deletingUsers = userDao.getAllUser();
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USER");

        if(deletingUsers.isEmpty()){
            return;
        }

        int userId = readInt(sc, "Enter User ID to delete (0 to cancel): ");

        if(userId == 0){
            System.out.println(" -> Operation Cancelled by the user.");
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