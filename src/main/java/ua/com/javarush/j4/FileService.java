package ua.com.javarush.j4;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileService {
    public void encryptFile(String filePath, int key) throws IOException {

        int lastDot = filePath.lastIndexOf('.');
        String encryptedPath = (lastDot != -1)
                ? filePath.substring(0, lastDot) + "[ENCRYPTED]" + filePath.substring(lastDot)
                : filePath + "[ENCRYPTED]";

        try (FileInputStream inputStream = new FileInputStream(filePath);
             FileOutputStream outputStream = new FileOutputStream(encryptedPath);
        ) {
            int bytesRead;
            while ((bytesRead = inputStream.read()) != -1) {
                char originalChar = (char) bytesRead;
                CaesarCipher caesarCipher = new CaesarCipher();
                char encryptedChar = caesarCipher.encryptChar(originalChar, key);
                outputStream.write((int) encryptedChar);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decryptFile(String filePath, int key) throws IOException {

        int lastDots = filePath.lastIndexOf('.');
        String decryptPath = (lastDots != -1)
                ? filePath.substring(0, lastDots) + "[DECRYPTED]" + filePath.substring(lastDots)
                : filePath + "[DECRYPTED]";

        try (FileInputStream inputStream = new FileInputStream(filePath);
             FileOutputStream outputStream = new FileOutputStream(decryptPath);
        ) {
            int bytesRead;
            while ((bytesRead = inputStream.read()) != -1) {
                char encryptedChar = (char) bytesRead;
                CaesarCipher caesarCipher = new CaesarCipher();
                char decryptedChar = caesarCipher.decryptChar(encryptedChar, key);
                outputStream.write((int) decryptedChar);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}