package com.scc.ui;

import java.util.Scanner;

public class InputHelper {

    public static int readInt(Scanner sc, String prompt){
        while(true){
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch(NumberFormatException e){
                System.out.println(" -> [ERROR] Invalid input. Numbers only.");
            }
        }
    }

    public static double readDouble(Scanner sc, String prompt){
        while(true){
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch(NumberFormatException e){
                System.out.println("Invalid input! Number lang (pananglitan 15.50).");
            }
        }
    }

    public static String readString(Scanner sc, String prompt){
        while(true){
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if(!input.isEmpty()){
                return input;
            }
            System.out.println(" -> [ERROR] Input cannot be empty.");
        }
    }

    public static String readPin(Scanner sc, String prompt){
        while(true){
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if(input.matches("\\d{4}")){
                return input;
            }
            System.out.println("PIN kinahanglan 4 ka digits lang.");
        }
    }

    public static int readIntRange(Scanner sc, String prompt, int min, int max){
        while(true){
            int value = readInt(sc, prompt);
            if(value >= min && value <= max){
                return value;
            }
            System.out.println("Pili ra gikan sa " + min + " hangtod " + max + ".");
        }
    }

    public static boolean readYesNo(Scanner sc, String prompt){
        while(true){
            System.out.print(prompt + " (Y/N): ");
            String input = sc.nextLine().trim().toLowerCase();
            if(input.equals("y") || input.equals("yes")) return true;
            if(input.equals("n") || input.equals("no")) return false;
            System.out.println("Please Enter Y or N.");
        }
    }
}