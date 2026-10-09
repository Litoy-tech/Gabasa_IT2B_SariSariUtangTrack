package com.scc.ui;

import java.util.Scanner;

public class InputHelper {

    // Integer (menu choice, ID, quantity)
    public static int readInt(Scanner sc, String prompt){
        while(true){
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch(NumberFormatException e){
                System.out.println("Invalid input! Numbers only, palihug.");
            }
        }
    }

    // Double (price)
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

    // String nga dili pwede blangko (name, username)
    public static String readString(Scanner sc, String prompt){
        while(true){
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if(!input.isEmpty()){
                return input;
            }
            System.out.println("Dili pwede nga blangko.");
        }
    }

    // 4-digit PIN
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

    // Integer nga naay minimum ug maximum (menu choice, quantity)
    public static int readIntRange(Scanner sc, String prompt, int min, int max){
        while(true){
            int value = readInt(sc, prompt);
            if(value >= min && value <= max){
                return value;
            }
            System.out.println("Pili ra gikan sa " + min + " hangtod " + max + ".");
        }
    }

    // Yes/No (pananglitan agreement on file, confirm delete)
    public static boolean readYesNo(Scanner sc, String prompt){
        while(true){
            System.out.print(prompt + " (Y/N): ");
            String input = sc.nextLine().trim().toLowerCase();
            if(input.equals("y") || input.equals("yes")) return true;
            if(input.equals("n") || input.equals("no")) return false;
            System.out.println("Y o N lang.");
        }
    }
}