package ua.com.javarush.gnew.file;

import java.nio.file.Path;

/**
 * Produces output filenames for encrypted and decrypted variants of a .txt input.
 * Assumes the input filename ends in ".txt"; behavior for other extensions is undefined.
 */
public class EncryptedFileNamer {

    private static final String EXTENSION = ".txt";
    private static final String ENCRYPTED_MARKER = " [ENCRYPTED]";
    private static final String DECRYPTED_MARKER = " [DECRYPTED]";

    public Path forEncrypted(Path input) {
        return input.resolveSibling(stripExtension(input) + ENCRYPTED_MARKER + EXTENSION);
    }

    public Path forDecrypted(Path input) {
        String base = stripExtension(input);
        if (base.endsWith(ENCRYPTED_MARKER)) {
            base = base.substring(0, base.length() - ENCRYPTED_MARKER.length());
        }
        return input.resolveSibling(base + DECRYPTED_MARKER + EXTENSION);
    }

    private String stripExtension(Path input) {
        String fileName = input.getFileName().toString();
        return fileName.substring(0, fileName.length() - EXTENSION.length());
    }
}
