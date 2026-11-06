package com.scc;

import com.scc.dao.AuditLogDao;
import com.scc.dao.DatabaseInitializer;
import com.scc.dao.ProductDao;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {


    public static void main(){

        DatabaseInitializer.initializer();
        Scanner sc = new Scanner(System.in);
        UserDao userDao = new UserDao();
        ProductDao productDao = new ProductDao();
        AuditLogDao auditLogDao = new AuditLogDao();

        Authentication auth = new Authentication(userDao, auditLogDao, productDao, sc);
        auth.start();
        sc.close();

    }
}