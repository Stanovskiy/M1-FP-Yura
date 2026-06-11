package com.java.additional.IO;

import java.util.Scanner;

public class CLI {

    private Scanner scanner;

    public String userCommand() {
        System.out.println("Write command, that mean what you wanna do\n" +
                "Available command [e]ncrypt, [d]ecrypt, [b]rute-force");
        String command = scanner.nextLine();
        if (command.equals("d")) {
            return "DECRYPT";
        } else if (command.equals("e")) {
            return "ENCRYPT";
        } else if (command.equals("b")) {
            return "BRUTE_FORCE";
        } else {
            throw new IllegalArgumentException("Unsupported command");
        }
    }

    public String userFilePath() {
        System.out.println("Write absolute path of your file");
        return scanner.nextLine();
    }

    public int userKey() {
        System.out.println("Write your key to use for your command");
        return scanner.nextInt();
    }
}