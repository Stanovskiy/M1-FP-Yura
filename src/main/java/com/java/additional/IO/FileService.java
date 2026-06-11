package com.java.additional.IO;

import com.java.additional.constants.Constants;
import com.java.additional.crypto.CaesarCipher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileService {
    private final String command;
    private final Path filePath;
    private int key;

    public FileService() {
        CLI cli = new CLI();
        command = cli.userCommand();
        filePath = srcCheck(cli.userFilePath());
        if (!command.equals("BRUTE_FORCE")) {
            key = cli.userKey();
        }
    }

    public FileService(String command, String filePath) {
        this.command = command;
        this.filePath = srcCheck(filePath);
    }

    public FileService(String command, String filePath, int key) {
        this(command, filePath);
        this.key = key;
    }

    public Path srcCheck(String path) {
        Path srcPath = Paths.get(path);
        if (!Files.exists(srcPath) && !srcPath.endsWith(".txt")) {
            throw new IllegalArgumentException("Specified file isn't exist or not belongs to .txt format");
        }
        return srcPath;
    }

    public static Path newFileCreation(Path oldFile, String prefix) {
        if (prefix.equals("BRUTE_FORCE")){
            prefix = "DECRYPT";
        }
        String fileName = oldFile.toString();
        int dotIndex = fileName.lastIndexOf(".");
        String nameWithoutExtension = fileName.substring(0, dotIndex);
        String extension = fileName.substring(dotIndex);

        String newFileName = nameWithoutExtension + "[" + prefix + "ED]" + extension;
        return Paths.get(newFileName);
    }


    public void processing() {
        Path targetFile = newFileCreation(filePath, command);
        String srcText;
        try {
            srcText = Files.readString(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean isEng = Constants.isEnglish(srcText);
        CaesarCipher cipher = new CaesarCipher();
        if (command.equals("ENCRYPT") || command.equals("DECRYPT")) {
            cipher.cipherWriter(srcText, key, command, targetFile);
        } else {
            cipher.brute(srcText, targetFile, isEng);
        }
    }
}