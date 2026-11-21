package com.scc.ui.systemAuditor;

import com.scc.dao.AuditLogDao;
import com.scc.dao.CustomerDao;
import com.scc.dao.ProductDao;
import com.scc.dao.SalesDao;
import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.Menu;

import static com.scc.util.InputHelper.*;

import java.util.*;

public class SystemAuditorMenu implements Menu {
    private final SystemAuditorService service;
    private final Scanner sc;

    public SystemAuditorMenu(UserDao userDao, CustomerDao customerDao, ProductDao productDao,
                             SalesDao salesDao, AuditLogDao logDao, Scanner sc) {
        this.service = new SystemAuditorService(userDao, customerDao, productDao, salesDao, logDao, sc);
        this.sc = sc;
    }

    @Override
    public void show(User SysAuditor) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("     SYSTEM AUDITOR MENU");
            System.out.println("===============================");
            System.out.println("Welcome, " + SysAuditor.fullName() + "!");

            System.out.println("\n[1] View Audit Log\n[2] View Sales\n[3] Daily Sales Report\n[4] Utang Report\n" +
                    "[5] View Payments\n[6] Best Sale\n" +
                    "[0] Logout");
            String choice = readString(sc, "Enter Choice: ");

            switch(choice){
                case "1" -> service.logHistory();
                case "2" -> service.viewSales();
                case "3" -> service.dailySalesReport(SysAuditor);
                case "4" -> service.utangReport(SysAuditor);
                case "5" -> service.viewPayments();
                case "6" -> service.bestSales();
                case "0" -> {return;}
                default -> System.out.println(" Enter 0-5.");
            }
        }
    }
}