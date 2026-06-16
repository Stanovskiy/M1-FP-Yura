package ua.com.javarush.j4;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Parser {

    private Mode mode;
    private Integer key;
    private Path file;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Path getFile() {
        return file;
    }

    public void setFile(Path file) {
        this.file = file;
    }

    public static Parser parse(String[] argument) {
        Parser args = new Parser();
        for (int i = 0; i < argument.length; i++) {
            switch (argument[i]) {
                case "-e"  -> args.setMode(Mode.ENCRYPT);
                case "-d"  -> args.setMode(Mode.DECRYPT);
                case "-bf" -> args.setMode(Mode.BRUTE_FORCE);
                case "-k"  -> args.setKey(Integer.parseInt(argument[++i]));
                case "-f"  -> args.setFile(Paths.get(argument[++i]));
                default    -> throw new IllegalArgumentException("Unknown arguments: " + argument[i]);
            }
        }
        return args;
    }
}