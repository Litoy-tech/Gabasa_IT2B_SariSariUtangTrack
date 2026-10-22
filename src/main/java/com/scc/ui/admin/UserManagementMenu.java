package com.scc.ui.admin;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.InputHelper;


import java.util.List;
import java.util.Scanner;

import static com.scc.ui.InputHelper.readInt;
import static com.scc.ui.InputHelper.readYesNo;

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
            System.out.println("[1] Add User\n[2] View All Users\n[3] View Pending User\n[5] Approved User" +
                    "\n[6] Deactivate User\n[0] Back");
            String choice = InputHelper.readString(sc, "Enter Choice: ");

            switch (choice) {
                case "1" -> System.out.println("Add User (soon)");
                case "2" -> viewAllUser();
                case "3" -> viewPendingUser();
                case "4" -> approvedUser();
                case "5" -> System.out.println("Padulong pa");
                case "6" -> {return;}
                default -> System.out.println("Invalid choice! 1 to 5 only.");
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

}