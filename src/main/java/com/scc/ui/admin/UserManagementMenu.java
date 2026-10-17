package com.scc.ui.admin;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.InputHelper;
import org.w3c.dom.ls.LSOutput;

import java.util.List;
import java.util.Scanner;

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
            System.out.println("[1] Add User\n[2] View All Users\n[3] Update User" +
                    "\n[4] Deactivate User\n[5] Back");
            String choice = InputHelper.readString(sc, "Enter Choice: ");

            switch (choice) {
                case "1" -> System.out.println("Add User (soon)");
                case "2" -> viewAllUser();

                case "3" -> System.out.println("Update User (soon)");
                case "4" -> System.out.println("Deactivate User (soon)");

                case "5" -> {return;}
                default -> System.out.println("Invalid choice! 1 to 5 only.");
            }
        }
    }

    private void displayUserTable(List<User> users, String title){
        System.out.println("\n================================= " +title+ " =================================");
        if(users.isEmpty()){
            System.out.println("\n-> No Records");
            return;
        }

        System.out.println("\n+------+--------------------+--------------------+--------------+----------+---------------------|");
        System.out.printf("| %-4s | %-18s | %-18s | %-12s | %-8s | %-19s |\n", "ID", "Fullname", "Username", "Role", "Status", "Date Created");
            for(User u: users){
                System.out.printf("\n| %-4s | %-18s | %-18s | %-12s | %-5s | %-15s |", u.user_id(), u.fullName(), u.username(), u.roles(), u.status(), u.created_at());
            }
        System.out.println("\n+------+--------------------+--------------------+--------------+----------+---------------------+");


    }

    private void viewAllUser(){
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USER");
    }

}
