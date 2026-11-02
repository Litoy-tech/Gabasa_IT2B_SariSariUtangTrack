package com.scc;

import com.scc.dao.AuditsDao;
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
        AuditsDao auditsDao = new AuditsDao();

        Authentication auth = new Authentication(userDao,productDao, auditsDao,sc);
        auth.start();
        sc.close();

    }
}