package com.scc.ui.Owner;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.InputHelper;
import com.scc.ui.Menu;

import java.util.Scanner;

public class OwnerMenu implements Menu {
    private final UserDao userdao;
    private final Scanner sc;


    public OwnerMenu(UserDao userdao,Scanner sc){
        this.userdao = userdao;
        this.sc = sc;
    }

    @Override
    public void show(User u){

        while(true){
            System.out.println("\n===== OWNER MENU =====");
            System.out.println("Welcome, " + u.fullName() + "!");
            System.out.println("======================");
            System.out.println("[1] Manage Product\n[2] Manage Customers\n[3] Manage Users\n[4] View Sales" +
                    "\n[5] Reports\n[6] Logout");
            int choice = InputHelper.readInt(sc, "Enter Choice: ");

            switch(choice){
                case 1: System.out.println("Manage Products (soon)"); break;
                case 2: System.out.println("Manage Customers (soon)"); break;
                case 3: System.out.println("Manage Users (soon)"); break;
                case 4: System.out.println("View Sales (soon)"); break;
                case 5: System.out.println("Reports (soon)"); break;
                case 6:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice! Pili ra gikan sa 1 hangtod 6.");
            }
        }
    }
}
