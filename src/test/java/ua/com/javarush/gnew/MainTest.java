package ua.com.javarush.gnew;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private static final boolean UKRAINIAN_LANGUAGE_TEST = false;
    private static final String ENCRYPT_COMMAND = "-e";
    private static final String DECRYPT_COMMAND = "-d";
    private static final String BF_COMMAND = "-bf";
    private static final String HAMLET_EN = loadResource("hamlet.txt");
    private static final String ORWELL_UA = loadResource("orwell.txt");

    private static String loadResource(String resourceName) {
        try {
            return Files.readString(Path.of(
                    MainTest.class.getResource("/" + resourceName).toURI()
            ));
        } catch (IOException | java.net.URISyntaxException e) {
            throw new RuntimeException("Failed to load test resource: " + resourceName, e);
        }
    }

    private Path inputFilePathEN;
    private Path inputFilePathUA;

    @TempDir
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        inputFilePathEN = createTestFile("EN_Text.txt", HAMLET_EN);
        inputFilePathUA = createTestFile("UA_Text.txt", ORWELL_UA);
    }

    private Path createTestFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, content);
        return filePath;
    }

    private Path execute(String command, Path inputFilePath, int key) {
        List<Path> filesBefore = listFiles(tempDir);
        List<String> params = List.of(command, "-k", String.valueOf(key), "-f", inputFilePath.toString());

        try {
            Main.main(params.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException("Execution failed", e);
        }

        return findNewFile(filesBefore);
    }

    private List<Path> listFiles(Path directory) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files in directory: " + directory, e);
        }
    }

    private Path findNewFile(List<Path> filesBefore) {
        List<Path> filesAfter = listFiles(tempDir);
        return filesAfter.stream()
                .filter(file -> !filesBefore.contains(file))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No new file was created"));
    }

    private String readFile(Path filePath) {
        Assumptions.assumeTrue(Files.exists(filePath), "File does not exist: " + filePath);
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            fail("Failed to read file: " + filePath, e);
            return null;
        }
    }

    @Nested
    @DisplayName("File tests")
    class FileTests {

        @Nested
        @DisplayName("ENCRYPT")
        class EncryptFileTests {

            @Test
            @DisplayName("File should be created")
            void encryptFileCreatingTest() {
                Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);

                assertTrue(Files.exists(encryptedFile), "Encrypted file was not created");
            }

            @Test
            @DisplayName("File should have a marker '[ENCRYPTED]'")
            void encryptFileMarkerTest() {
                Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);

                assertTrue(encryptedFile.getFileName().toString().contains("[ENCRYPTED]"),
                        "Encrypted file doesn't have '[ENCRYPTED]' marker. File name: " + encryptedFile.getFileName());
            }
        }

        @Nested
        @DisplayName("DECRYPT")
        class DecryptFileTests {

            @Test
            @DisplayName("File should be created")
            void decryptedFileCreatingTest() {
                Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
                Path decryptedFile = execute(DECRYPT_COMMAND, encryptedFile, 5);

                assertTrue(Files.exists(decryptedFile), "Decrypted file was not created");
            }

            @Test
            @DisplayName("File should have a marker '[DECRYPTED]'")
            void decryptedFileMarkerTest() {
                Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
                Path decryptedFile = execute(DECRYPT_COMMAND, encryptedFile, 5);

                assertTrue(decryptedFile.getFileName().toString().contains("[DECRYPTED]"),
                        "Decrypted file doesn't have '[DECRYPTED]' marker. File name: " + decryptedFile.getFileName());
            }
        }

        @Nested
        @DisplayName("BRUTE FORCE")
        class BruteForceFileTests {

            @Test
            @DisplayName("File should be created")
            void decryptedFileCreatingTest() {
                Path bruteForcedFile = execute(BF_COMMAND, inputFilePathEN, 5);

                assertTrue(Files.exists(bruteForcedFile), "Decrypted file was not created");
            }
        }
    }

    @Nested
    @DisplayName("English language tests")
    class EnglishTests {

        @DisplayName("[ENCRYPT] Simple letters encoding")
        @ParameterizedTest
        @CsvSource({"A, 1, B", "a, 1, b", "A, 25, Z", "a, 25, z"})
        void encrypt(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, key);

            String encryptedText = readFile(encryptedFile);
            assertEquals(expected, encryptedText);
        }

        @DisplayName("[DECRYPT] Simple letters decoding")
        @ParameterizedTest
        @CsvSource({"B, 1, A", "b, 1, a", "Z, 25, A", "z, 25, a"})
        void decrypt(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path decryptedFile = execute(DECRYPT_COMMAND, testFile, key);

            String decryptedText = readFile(decryptedFile);
            assertEquals(expected, decryptedText);
        }

        @Test
        @DisplayName("[DECRYPT] Decrypted text should be equal to the original.")
        void decryptedFileTextValidate() {
            Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
            Path decryptedFile = execute(DECRYPT_COMMAND, encryptedFile, 5);

            String decryptedText = readFile(decryptedFile);
            assertEquals(HAMLET_EN, decryptedText, "Decrypted text is not the same as original");
        }

        @Test
        @DisplayName("[BRUTE FORCE] Decrypted text should be equal to the original.")
        void bruteForceEN() {
            Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
            Path bruteForcedFile = execute(BF_COMMAND, encryptedFile, 5);

            String bruteForcedText = readFile(bruteForcedFile);

            assertEqualsIgnoreCase(HAMLET_EN, bruteForcedText, "Decrypted text is not the same");
        }

        private void assertEqualsIgnoreCase(String expected, String actual, String message) {
            assertTrue(expected.equalsIgnoreCase(actual), message);
            assertEquals(expected, actual, message);
        }
    }

    @Nested
    @DisplayName("Ukrainian Language Tests")
    @EnabledIf("isUkrainianLanguageTestEnabled")
    class UkrainianLanguageTest {

        private static boolean isUkrainianLanguageTestEnabled() {
            return UKRAINIAN_LANGUAGE_TEST;
        }

        @DisplayName("[ENCRYPT] Simple letters encoding")
        @ParameterizedTest
        @CsvSource({"А, 1, Б", "а, 1, б", "А, 32, Я", "а, 32, я"})
        void encrypt(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, key);

            String encryptedText = readFile(encryptedFile);
            assertEquals(expected, encryptedText);
        }

        @DisplayName("[DECRYPT] Simple letters decryption")
        @ParameterizedTest
        @CsvSource({"Б, 1, А", "б, 1, а", "Я, 32, А", "я, 32, а"})
        void decrypt(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path decryptedFile = execute(DECRYPT_COMMAND, testFile, key);

            String decryptedText = readFile(decryptedFile);
            assertEquals(expected, decryptedText);
        }

        @Test
        @DisplayName("[DECRYPT] Decrypted text should be equal to the original.")
        void decryptTestUA() {
            Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathUA, 5);
            Path decryptedFile = execute(DECRYPT_COMMAND, encryptedFile, 5);
            String decryptedText = readFile(decryptedFile);

            assertEquals(ORWELL_UA, decryptedText, "Decrypted text is not the same as original");
        }

        @Test
        @DisplayName("[BRUTE FORCE] Decrypted text should be equal to the original.")
        void bruteForceTestUA() {
            Path encryptedFile = execute(BF_COMMAND, inputFilePathUA, 5);
            Path bruteForcedFile = execute(BF_COMMAND, encryptedFile, 5);
            String decryptedText = readFile(bruteForcedFile);

            assertEquals(ORWELL_UA, decryptedText, "Decrypted text using brute force is not the same as original");
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @DisplayName("Negative key should be validated")
        @ParameterizedTest
        @CsvSource({"A, -1, Z", "a, -1, z", "Z, -25, A", "z, -25, a"})
        void negativeKeyEncryption(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, key);

            String encryptedText = readFile(encryptedFile);
            assertEquals(expected, encryptedText);
        }

        @Test
        @DisplayName("File not exists exception should be handled")
        void fileNotExists() {
            Path fakeFilePath = Path.of("/fake/path/file.txt");

            String[] params = {ENCRYPT_COMMAND, "-f", fakeFilePath.toString(), "-k", "5"};

            assertDoesNotThrow(() -> Main.main(params), "Exception was thrown while processing a non-existent file path.");
        }
    }
}
