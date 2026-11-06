package com.scc.ui.systemAuditor;

import com.scc.dao.AuditLogDao;
import com.scc.dao.UserDao;
import com.scc.model.Audit;
import com.scc.model.User;
import com.scc.ui.Menu;
import static com.scc.ui.InputHelper.*;

import java.util.List;
import java.util.Scanner;

public class SystemAuditorMenu implements Menu {
    private final UserDao userDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

    public SystemAuditorMenu(UserDao userdao, AuditLogDao logDao, Scanner sc) {
        this.userDao = userdao;
        this.logDao = logDao;
        this.sc = sc;
    }

    @Override
    public void show(User SysAuditor) {
        System.out.println("\n===============================");
        System.out.println("     SYSTEM AUDITOR MENU");
        System.out.println("===============================");
        System.out.println("Welcome, " + SysAuditor.fullName() + "!");

        while (true) {
            System.out.println("\n[1] View Audit Log\n[2] View Sales\n[3] Daily Sales Report\n[4] Utang Report\n" +
                    "[5] Export Reports (PDF / CSV)\n" +
                    "[0] Logout");
            String choice = readString(sc, "Enter Choice: ");

            switch(choice){
                case "1" -> logHistory();
                case "2" -> System.out.println("View Sales (soon)");
                case "3" -> System.out.println("Daily Sales Report (soon)");
                case "4" -> System.out.println("Utang Report (soon)");
                case "5" -> System.out.println("Export Reports (soon)");
                case "0" -> {return;}
                default -> System.out.println(" Enter 0-5.");
            }
        }
    }

    private void logHistory(){
        List<Audit> auditsLogs = logDao.getAllLogs();
        displayTable(auditsLogs, "AUDIT LOG");
    }

    private void displayTable(List<Audit> logs, String title){
        System.out.println("\n===========================================================================================================================================================");
        System.out.println("                                         " + title + " ");
        if(logs.isEmpty()){
            System.out.println("\n-> No Records.");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        System.out.println("+------+---------------------+----------------------+------------------------+--------------+--------+----------------------------------------------------+");
        System.out.printf ("| %-4s | %-19s | %-20s | %-22s | %-12s | %-6s | %-50s |\n", "ID", "Timestamp", "User", "Action", "Table", "Rec ID", "Description");
        System.out.println("+------+---------------------+----------------------+------------------------+--------------+--------+----------------------------------------------------+");
        for(Audit a : logs){
            String table = (a.table_affected() == null) ? "-" : a.table_affected();
            String recId = (a.record_id() == null) ? "-" : String.valueOf(a.record_id());
            System.out.printf("| %-4d | %-19s | %-20s | %-22s | %-12s | %-6s | %-50s |\n",
                    a.log_id(), a.log_stamp(), a.username(), a.action(), table, recId, a.description());
        }
        System.out.println("+------+---------------------+----------------------+------------------------+--------------+--------+----------------------------------------------------+");
        System.out.println(" Total logs: " + logs.size());
    }
}