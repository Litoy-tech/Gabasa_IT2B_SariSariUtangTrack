package com.scc;

import com.scc.dao.*;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {


    public static void main(){

        DatabaseInitializer.initializer();
        Scanner sc = new Scanner(System.in);
        UserDao userDao = new UserDao();
        CustomerDao customerDao = new CustomerDao();
        ProductDao productDao = new ProductDao();
        AuditLogDao auditLogDao = new AuditLogDao();

        Authentication auth = new Authentication(userDao, customerDao, productDao, auditLogDao, sc);
        auth.start();
        sc.close();

    }
}