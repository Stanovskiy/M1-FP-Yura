package ua.com.javarush.gnew.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncryptedFileNamerTest {

    private final EncryptedFileNamer namer = new EncryptedFileNamer();

    @Nested
    @DisplayName("forEncrypted")
    class ForEncrypted {

        @Test
        @DisplayName("appends ' [ENCRYPTED]' before .txt")
        void appendsEncryptedMarker() {
            Path result = namer.forEncrypted(Paths.get("/tmp/foo.txt"));
            assertEquals(Paths.get("/tmp/foo [ENCRYPTED].txt"), result);
        }

        @Test
        @DisplayName("preserves the parent directory")
        void preservesParent() {
            Path result = namer.forEncrypted(Paths.get("/a/b/c/file.txt"));
            assertEquals(Paths.get("/a/b/c/file [ENCRYPTED].txt"), result);
        }
    }

    @Nested
    @DisplayName("forDecrypted")
    class ForDecrypted {

        @Test
        @DisplayName("replaces [ENCRYPTED] with [DECRYPTED] when present")
        void replacesEncryptedMarker() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo [ENCRYPTED].txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
        }

        @Test
        @DisplayName("appends ' [DECRYPTED]' before .txt when no marker present")
        void appendsWhenNoMarker() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo.txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
        }

        @Test
        @DisplayName("does not double-suffix [ENCRYPTED] [DECRYPTED]")
        void noDoubleSuffix() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo [ENCRYPTED].txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
        }
    }
}
