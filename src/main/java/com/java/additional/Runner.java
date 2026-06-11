package com.java.additional;

import com.java.additional.IO.FileService;

public class Runner {

    private FileService defineFSConstructor(String[] args) {
        if (args.length > 3) {
            throw new IllegalArgumentException("The number of arguments is greater than 3, check that the path to your file is correct!");
        } else if (args.length == 3) {
            return new FileService(args[0], args[1], Integer.parseInt(args[2]));
        } else if (args.length == 2) {
            if (args[1].equals("BRUTE_FORCE")) {
                return new FileService(args[0], args[1]);
            } else {
                throw new IllegalArgumentException("Unsupported command");
            }
        } else {
            return new FileService();
        }
    }

    public void run(String[] args) {
        FileService fileService = defineFSConstructor(args);
        fileService.processing();
    }
}