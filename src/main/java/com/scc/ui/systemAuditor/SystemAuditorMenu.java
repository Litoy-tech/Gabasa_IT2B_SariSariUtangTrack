package com.scc.ui.systemAuditor;

import com.scc.dao.UserDao;
import com.scc.model.Audit;
import com.scc.model.User;
import com.scc.ui.Menu;
import static com.scc.ui.InputHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SystemAuditorMenu implements Menu {
    private final UserDao userDao;
    private final Scanner sc;

    public SystemAuditorMenu(UserDao userdao,Scanner sc) {
        this.userDao = userdao;
        this.sc = sc;
    }

    @Override
    public void show(User SysAuditor) {
        System.out.println("\n===============================");
        System.out.println("     SYSTEM AUDITOR MENU");
        System.out.println("===============================");
        System.out.println("Welcome, " + SysAuditor.fullName() + "!");

        while (true) {
            System.out.println("[1] View Audit Log\n[2] View Sales\n[3] Daily Sales Report\n[4] Utang Report\n" +
                    "[5] Export Reports (PDF / CSV)\n" +
                    "[0] Logout");
            String choice = readString(sc, "Enter Choice: ");

            switch(choice){
                case "1" -> System.out.println("View Audit");
                case "0" -> {return;}
                default -> System.out.println(" Enter 0-5.");
            }
        }
    }

    private void displayTable(){

    }

    private void logHistory(){
        List<Audit> auditsLogs = new ArrayList<>();
    }
}