package ua.com.javarush.j4;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        try {
            Parser count = Parser.parse(args);

            if (count.getMode() == null) {
                throw new IllegalArgumentException("Немає ніяких аргументів в Майні");
            }
            if (count.getFile() == null) {
                throw new IllegalArgumentException("Missing -f (file path). Please specify file");
            }
            if (count.getMode() != Mode.BRUTE_FORCE && count.getKey() == null) {
                throw new IllegalArgumentException("Missing -k (shift key)");
            }
            if (!Files.exists(count.getFile())) {
                throw new IllegalArgumentException("File not found: " + count.getFile());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        FileService fileService = new FileService();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter command (1.ENCRYPT/Зашифровать 2. DECRYPT/Дешифровка 3. BRUTE_FORCE):");
        int command = scanner.nextInt();
        System.out.println("Enter input file path:");
        Scanner scannera = new Scanner(System.in);
        String filePath = scannera.nextLine();

        try {
            if (command == 1 || command == 2) {
                System.out.println("Enter key:");
                int key = scanner.nextInt();
                if (command == 1) {
                    fileService.encryptFile(filePath, key);
                } else {
                    fileService.decryptFile(filePath, key);
                }
            } else if (command == 3) {
                BruteForce bruteForces = new BruteForce();
                bruteForces.bruteForce(filePath);
            } else {
                System.out.println("Invalid command.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }

    }
}
