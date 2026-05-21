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

/**
 * Інтеграційні тести програми. Кожен тест запускає {@code Main.main(...)}
 * у тимчасовій директорії ({@link TempDir}) і перевіряє створений файл.
 *
 * <p>У підкоментарях до кожного тесту вказано:
 * <ul>
 *   <li><b>Що перевіряє</b> — яка частина функціонала тестується;</li>
 *   <li><b>Як пройти</b> — що саме треба реалізувати, щоб тест став зеленим.</li>
 * </ul>
 */
class MainTest {
    private static final String ENCRYPT_COMMAND = "-e";
    private static final String DECRYPT_COMMAND = "-d";
    private static final String BF_COMMAND = "-bf";
    private static final String HAMLET_EN = loadResource("hamlet.txt");
    private static final String ORWELL_UA = loadResource("orwell.txt");

    private static String loadResource(String resourceName) {
        try (var in = MainTest.class.getResourceAsStream("/" + resourceName)) {
            if (in == null) {
                throw new RuntimeException("Test resource not found on classpath: " + resourceName);
            }
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
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
    @DisplayName("Перевірка створення файлів та маркерів в імені")
    class FileTests {

        @Nested
        @DisplayName("ШИФРУВАННЯ")
        class EncryptFileTests {

            /**
             * <b>Що перевіряє:</b> після виклику {@code -e -k <ключ> -f <файл.txt>}
             * у тій самій папці з'являється новий файл (зашифрована версія).
             *
             * <p><b>Як пройти:</b> функціонал уже реалізовано в {@code Main.ENCRYPT}.
             * Тест має бути зеленим одразу.
             */
            @Test
            @DisplayName("Після шифрування створюється новий файл")
            void encryptFileCreatingTest() {
                Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);

                assertTrue(Files.exists(encryptedFile), "Encrypted file was not created");
            }

            /**
             * <b>Що перевіряє:</b> ім'я зашифрованого файлу містить мітку {@code [ENCRYPTED]}
             * (наприклад, {@code foo.txt} → {@code foo [ENCRYPTED].txt}).
             *
             * <p><b>Як пройти:</b> {@code Main} вже використовує {@code EncryptedFileNamer.forEncrypted(...)},
             * який додає мітку перед {@code .txt}. Тест має бути зеленим одразу.
             */
            @Test
            @DisplayName("Ім'я файлу містить мітку '[ENCRYPTED]'")
            void encryptFileMarkerTest() {
                Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);

                assertTrue(encryptedFile.getFileName().toString().contains("[ENCRYPTED]"),
                        "Encrypted file doesn't have '[ENCRYPTED]' marker. File name: " + encryptedFile.getFileName());
            }
        }

        @Nested
        @DisplayName("РОЗШИФРУВАННЯ")
        class DecryptFileTests {

            /**
             * <b>Що перевіряє:</b> після виклику {@code -d -k <ключ> -f <файл [ENCRYPTED].txt>}
             * у папці з'являється новий файл (розшифрована версія).
             *
             * <p><b>Як пройти:</b> реалізуй {@code Cypher.decrypt(input, key)}.
             * Гілка {@code DECRYPT} у {@code Main} вже викликає його і записує результат у файл.
             * Найпростіша реалізація {@code decrypt}: викликати {@code encrypt(input, -key)} —
             * шифрування з протилежним ключем якраз і скасовує початковий зсув.
             */
            @Test
            @DisplayName("Після розшифрування створюється новий файл")
            void decryptedFileCreatingTest() {
                Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
                Path decryptedFile = execute(DECRYPT_COMMAND, encryptedFile, 5);

                assertTrue(Files.exists(decryptedFile), "Decrypted file was not created");
            }

            /**
             * <b>Що перевіряє:</b> ім'я розшифрованого файлу містить мітку {@code [DECRYPTED]}.
             *
             * <p><b>Як пройти:</b> те саме, що для попереднього тесту — реалізуй {@code Cypher.decrypt}.
             * {@code EncryptedFileNamer.forDecrypted(...)} автоматично сформує правильне ім'я.
             */
            @Test
            @DisplayName("Ім'я розшифрованого файлу містить мітку '[DECRYPTED]'")
            void decryptedFileMarkerTest() {
                Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
                Path decryptedFile = execute(DECRYPT_COMMAND, encryptedFile, 5);

                assertTrue(decryptedFile.getFileName().toString().contains("[DECRYPTED]"),
                        "Decrypted file doesn't have '[DECRYPTED]' marker. File name: " + decryptedFile.getFileName());
            }
        }

        @Nested
        @DisplayName("ПЕРЕБІР КЛЮЧІВ (BRUTE FORCE)")
        class BruteForceFileTests {

            /**
             * <b>Що перевіряє:</b> після виклику {@code -bf -f <зашифрований файл>}
             * у папці з'являється новий файл — розшифрований без вказаного ключа.
             *
             * <p><b>Як пройти:</b> реалізуй {@code BruteForce.bruteForce(cipherText)}.
             * Гілка {@code BRUTEFORCE} у {@code Main} вже викликає його.
             * Підказка: перебери всі можливі ключі (1..25 для англійського алфавіту), для кожного
             * варіанту обчисли «оцінку схожості на справжній текст», вибери варіант з найкращою оцінкою.
             */
            @Test
            @DisplayName("Після brute-force створюється новий файл")
            void decryptedFileCreatingTest() {
                Path bruteForcedFile = execute(BF_COMMAND, inputFilePathEN, 5);

                assertTrue(Files.exists(bruteForcedFile), "Decrypted file was not created");
            }
        }

        @Nested
        @DisplayName("Перетворення імені файлу при розшифруванні")
        class DecryptFilenameTransformation {

            /**
             * <b>Що перевіряє:</b> при розшифруванні {@code foo [ENCRYPTED].txt} нове ім'я
             * має бути {@code foo [DECRYPTED].txt}, а не {@code foo [ENCRYPTED] [DECRYPTED].txt}.
             * Тобто мітку треба ЗАМІНИТИ, а не додати.
             *
             * <p><b>Як пройти:</b> {@code EncryptedFileNamer.forDecrypted(...)} уже коректно
             * замінює мітку. Потрібно лише реалізувати {@code Cypher.decrypt}, щоб тест
             * взагалі дійшов до перевірки імені.
             */
            @Test
            @DisplayName("'[ENCRYPTED]' замінюється на '[DECRYPTED]', а не додається поряд")
            void replacesEncryptedMarker() {
                Path encrypted = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
                Path decrypted = execute(DECRYPT_COMMAND, encrypted, 5);
                String decryptedName = decrypted.getFileName().toString();
                assertTrue(decryptedName.contains("[DECRYPTED]"),
                        "Expected '[DECRYPTED]' in name: " + decryptedName);
                assertFalse(decryptedName.contains("[ENCRYPTED]"),
                        "Decrypted file should not still carry '[ENCRYPTED]' marker: " + decryptedName);
            }
        }
    }

    @Nested
    @DisplayName("Тести англійською мовою")
    class EnglishTests {

        /**
         * <b>Що перевіряє:</b> базовий зсув літер англійського алфавіту:
         * A+1=B, a+1=b, A+25=Z, a+25=z.
         *
         * <p><b>Як пройти:</b> функціонал {@code Cypher.encrypt} уже реалізовано — тест зелений одразу.
         */
        @DisplayName("[ШИФРУВАННЯ] Окремі літери: A+1=B, a+1=b, A+25=Z, a+25=z")
        @ParameterizedTest
        @CsvSource({"A, 1, B", "a, 1, b", "A, 25, Z", "a, 25, z"})
        void encrypt(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, key);

            String encryptedText = readFile(encryptedFile);
            assertEquals(expected, encryptedText);
        }

        /**
         * <b>Що перевіряє:</b> базове розшифрування — зворотній зсув:
         * B-1=A, b-1=a, Z-25=A, z-25=a.
         *
         * <p><b>Як пройти:</b> реалізуй {@code Cypher.decrypt(input, key)}.
         * Найпростіше: {@code return encrypt(input, -key);} — зсув з протилежним знаком.
         */
        @DisplayName("[РОЗШИФРУВАННЯ] Окремі літери: B-1=A, b-1=a, Z-25=A, z-25=a")
        @ParameterizedTest
        @CsvSource({"B, 1, A", "b, 1, a", "Z, 25, A", "z, 25, a"})
        void decrypt(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path decryptedFile = execute(DECRYPT_COMMAND, testFile, key);

            String decryptedText = readFile(decryptedFile);
            assertEquals(expected, decryptedText);
        }

        /**
         * <b>Що перевіряє:</b> повний цикл: великий текст («Гамлет», англ.) шифрується,
         * потім розшифровується тим самим ключем — результат має дорівнювати оригіналу.
         *
         * <p><b>Як пройти:</b> реалізуй {@code Cypher.decrypt} так, щоб для будь-якого тексту
         * і ключа справджувалось {@code decrypt(encrypt(text, k), k) == text}.
         */
        @Test
        @DisplayName("[РОЗШИФРУВАННЯ] Цикл encrypt→decrypt повертає оригінал (Гамлет)")
        void decryptedFileTextValidate() {
            Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
            Path decryptedFile = execute(DECRYPT_COMMAND, encryptedFile, 5);

            String decryptedText = readFile(decryptedFile);
            assertEquals(HAMLET_EN, decryptedText, "Decrypted text is not the same as original");
        }

        /**
         * <b>Що перевіряє:</b> brute-force знаходить правильний ключ самостійно
         * і відновлює англійський текст з шифру (порівняння точне, з урахуванням регістру).
         *
         * <p><b>Як пройти:</b> реалізуй {@code BruteForce.bruteForce(cipherText)}.
         * Підхід 1 — словниковий: спробуй кожен ключ 1..25, у результаті порахуй кількість
         * частих англійських слів («the», «and», «of», «is» тощо), вибери варіант із
         * найбільшою кількістю. Підхід 2 — частотний аналіз: порівняй розподіл частот
         * літер у розшифрованому тексті з еталонним розподілом англійської мови.
         */
        @Test
        @DisplayName("[ПЕРЕБІР КЛЮЧІВ] Знаходить ключ і повертає оригінальний текст (Гамлет)")
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
    @DisplayName("Шифрування: межові випадки")
    class EncryptEdgeCases {

        /**
         * <b>Що перевіряє:</b> шифрування порожнього файлу повертає порожній файл.
         *
         * <p><b>Як пройти:</b> уже працює — порожній рядок проходить через цикл без жодної ітерації.
         */
        @Test
        @DisplayName("Порожній файл → порожній зашифрований файл")
        void emptyFile() throws IOException {
            Path testFile = createTestFile("empty.txt", "");
            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 5);
            assertEquals("", readFile(encryptedFile));
        }

        /**
         * <b>Що перевіряє:</b> файл з єдиною літерою 'A' з ключем 1 → 'B'.
         *
         * <p><b>Як пройти:</b> уже працює — базовий випадок шифрування.
         */
        @Test
        @DisplayName("Один символ: 'A' з ключем 1 → 'B'")
        void singleLetter() throws IOException {
            Path testFile = createTestFile("single.txt", "A");
            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 1);
            assertEquals("B", readFile(encryptedFile));
        }

        /**
         * <b>Що перевіряє:</b> цифри (0–9) не входять до алфавіту, тому шифр їх не змінює.
         *
         * <p><b>Як пройти:</b> уже працює — {@code Cypher.processSymbol} пропускає символи,
         * яких немає у {@code originalAlphabet}, без змін.
         */
        @Test
        @DisplayName("Файл лише з цифрами не змінюється шифруванням")
        void digitsOnly() throws IOException {
            Path testFile = createTestFile("digits.txt", "0123456789");
            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 5);
            assertEquals("0123456789", readFile(encryptedFile));
        }

        /**
         * <b>Що перевіряє:</b> ключ 0 — це нульовий зсув, текст не змінюється.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Ключ 0 — текст не змінюється")
        void keyZero() throws IOException {
            Path testFile = createTestFile("k0.txt", "Hello, World!");
            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 0);
            assertEquals("Hello, World!", readFile(encryptedFile));
        }

        /**
         * <b>Що перевіряє:</b> ключ 52 = повний оберт по 52-символьному алфавіту (A..Z + a..z),
         * тому результат тотожний оригіналу.
         *
         * <p><b>Як пройти:</b> уже працює — {@code Collections.rotate} коректно обробляє
         * зсув, кратний розміру списку.
         */
        @Test
        @DisplayName("Ключ 52 (повне коло алфавіту) — текст не змінюється")
        void keyFullCycle() throws IOException {
            Path testFile = createTestFile("k52.txt", "Hello");
            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 52);
            assertEquals("Hello", readFile(encryptedFile));
        }

        /**
         * <b>Що перевіряє:</b> ключі більші за розмір алфавіту нормалізуються по колу:
         * 53 mod 52 = 1, тому ключ 53 дає той самий результат, що ключ 1.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Ключ 53 дає той самий результат, що ключ 1")
        void keyOverCycle() throws IOException {
            Path withK1 = execute(ENCRYPT_COMMAND, createTestFile("k1.txt", "Hello"), 1);
            Path withK53 = execute(ENCRYPT_COMMAND, createTestFile("k53.txt", "Hello"), 53);
            assertEquals(readFile(withK1), readFile(withK53));
        }

        /**
         * <b>Що перевіряє:</b> від'ємний ключ -52 = повний оберт у зворотному напрямку,
         * текст не змінюється.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Ключ -52 (повне коло назад) — текст не змінюється")
        void keyNegativeFullCycle() throws IOException {
            Path testFile = createTestFile("kneg52.txt", "Hello");
            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, -52);
            assertEquals("Hello", readFile(encryptedFile));
        }

        /**
         * <b>Що перевіряє:</b> розділові знаки і пробіли ({@code .,!? \t}) не входять до
         * алфавіту і проходять без змін.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Розділові знаки і пробіли проходять без змін")
        void specialCharsPassThrough() throws IOException {
            Path testFile = createTestFile("special.txt", ".,!? \t");
            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 5);
            assertEquals(".,!? \t", readFile(encryptedFile));
        }

        /**
         * <b>Що перевіряє:</b> перенесення рядків ({@code \n}) зберігаються, а літери
         * на різних рядках шифруються незалежно: «abc\ndef\n» з ключем 1 → «bcd\nefg\n».
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Багаторядковий вміст зберігає переходи рядків")
        void multilineContent() throws IOException {
            Path testFile = createTestFile("multi.txt", "abc\ndef\n");
            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 1);
            assertEquals("bcd\nefg\n", readFile(encryptedFile));
        }
    }

    @Nested
    @DisplayName("Збереження початкового файлу")
    class OriginalFileSafety {

        /**
         * <b>Що перевіряє:</b> після виклику {@code -e} оригінальний вхідний файл на диску
         * залишається таким, як був — програма не перезаписує його, а створює окремий файл.
         *
         * <p><b>Як пройти:</b> уже працює — {@code Main} пише результат у новий шлях
         * (через {@code EncryptedFileNamer}), а вхідний файл лише читає.
         */
        @Test
        @DisplayName("Шифрування не змінює вхідний файл")
        void encryptDoesNotModifyInput() throws IOException {
            String original = "Hello, World!";
            Path testFile = createTestFile("safety.txt", original);
            execute(ENCRYPT_COMMAND, testFile, 5);
            assertEquals(original, Files.readString(testFile));
        }
    }

    @Nested
    @DisplayName("Тести українською мовою")
    @EnabledIf("isUkrainianLanguageTestEnabled")
    class UkrainianLanguageTest {

        private static boolean isUkrainianLanguageTestEnabled() {
            // Enable via: mvn -DukrainianLanguageTest=true test
            return true;
        }

        /**
         * <b>Що перевіряє:</b> шифрування українських літер: А+1=Б, а+1=б, А+32=Я, а+32=я
         * (32 = повний оберт по 33-літерному українському алфавіту мінус одна літера).
         *
         * <p><b>Як пройти:</b> створи клас {@code UkrainianLanguage extends Language} з
         * 33-літерним алфавітом (А Б В Г Ґ Д Е Є Ж З И І Ї Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш Щ Ь Ю Я
         * плюс відповідні малі літери). Потім переробіть {@code Cypher}, щоб він приймав
         * {@code Language} через конструктор (або параметром у {@code encrypt}/{@code decrypt}),
         * і навчіть {@code Main} визначати мову тексту.
         */
        @DisplayName("[ШИФРУВАННЯ UA] Українські літери: А+1=Б, а+1=б, А+32=Я, а+32=я")
        @ParameterizedTest
        @CsvSource({"А, 1, Б", "а, 1, б", "А, 32, Я", "а, 32, я"})
        void encrypt(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, key);

            String encryptedText = readFile(encryptedFile);
            assertEquals(expected, encryptedText);
        }

        /**
         * <b>Що перевіряє:</b> розшифрування українських літер — зворотній зсув:
         * Б-1=А, б-1=а, Я-32=А, я-32=а.
         *
         * <p><b>Як пройти:</b> та сама підтримка українського алфавіту, що для попереднього
         * тесту, плюс реалізація {@code Cypher.decrypt}.
         */
        @DisplayName("[РОЗШИФРУВАННЯ UA] Окремі літери: Б-1=А, б-1=а, Я-32=А, я-32=а")
        @ParameterizedTest
        @CsvSource({"Б, 1, А", "б, 1, а", "Я, 32, А", "я, 32, а"})
        void decrypt(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path decryptedFile = execute(DECRYPT_COMMAND, testFile, key);

            String decryptedText = readFile(decryptedFile);
            assertEquals(expected, decryptedText);
        }

        /**
         * <b>Що перевіряє:</b> повний цикл encrypt→decrypt над уривком «1984» Орвелла
         * (укр.) повертає оригінальний текст.
         *
         * <p><b>Як пройти:</b> 1) підтримка українського алфавіту в {@code Cypher};
         * 2) реалізація {@code Cypher.decrypt}; 3) {@code Main} має визначити, який
         * алфавіт використовувати для конкретного файлу (за вмістом або за прапорцем).
         */
        @Test
        @DisplayName("[РОЗШИФРУВАННЯ UA] Цикл encrypt→decrypt повертає оригінал (Орвелл, '1984')")
        void decryptTestUA() {
            Path encryptedFile = execute(ENCRYPT_COMMAND, inputFilePathUA, 5);
            Path decryptedFile = execute(DECRYPT_COMMAND, encryptedFile, 5);
            String decryptedText = readFile(decryptedFile);

            assertEquals(ORWELL_UA, decryptedText, "Decrypted text is not the same as original");
        }

        /**
         * <b>Що перевіряє:</b> brute-force знаходить ключ і відновлює український текст
         * без ключа (на вході — заздалегідь зашифрований український уривок).
         *
         * <p><b>Як пройти:</b> 1) підтримка українського алфавіту;
         * 2) {@code BruteForce.bruteForce} має враховувати мову при оцінюванні —
         * для української треба порівнювати з частотами літер або з частими словами
         * саме української, не англійської.
         */
        @Test
        @DisplayName("[ПЕРЕБІР КЛЮЧІВ UA] Знаходить ключ і повертає оригінальний український текст")
        void bruteForceTestUA() {
            Path encryptedFile = execute(BF_COMMAND, inputFilePathUA, 5);
            Path bruteForcedFile = execute(BF_COMMAND, encryptedFile, 5);
            String decryptedText = readFile(bruteForcedFile);

            assertEquals(ORWELL_UA, decryptedText, "Decrypted text using brute force is not the same as original");
        }
    }

    @Nested
    @DisplayName("Валідація вхідних даних")
    class ValidationTests {

        /**
         * <b>Що перевіряє:</b> шифрування з від'ємним ключем коректно зсуває в зворотному
         * напрямку з циклічним переходом через межу регістру:
         * A-1=z (зворот через увесь алфавіт), a-1=Z, Z-25=A, z-25=a.
         *
         * <p><b>Як пройти:</b> уже працює — алгоритм у {@code Cypher} підтримує від'ємні ключі.
         */
        @DisplayName("Шифрування з від'ємним ключем коректно зсуває по колу")
        @ParameterizedTest
        @CsvSource({"A, -1, z", "a, -1, Z", "Z, -25, A", "z, -25, a"})
        void negativeKeyEncryption(String input, int key, String expected) throws IOException {
            Path testFile = createTestFile("testFile.txt", input);

            Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, key);

            String encryptedText = readFile(encryptedFile);
            assertEquals(expected, encryptedText);
        }

        /**
         * <b>Що перевіряє:</b> якщо вказаний у {@code -f} файл не існує, програма не
         * падає з винятком, а коректно завершується.
         *
         * <p><b>Як пройти:</b> уже працює — {@code Main.main} обгорнуто в {@code try-catch},
         * який ловить будь-який виняток і виводить повідомлення.
         */
        @Test
        @DisplayName("Програма не падає, якщо вхідний файл не існує")
        void fileNotExists() {
            Path fakeFilePath = Path.of("/fake/path/file.txt");

            String[] params = {ENCRYPT_COMMAND, "-f", fakeFilePath.toString(), "-k", "5"};

            assertDoesNotThrow(() -> Main.main(params), "Exception was thrown while processing a non-existent file path.");
        }

        /**
         * <b>Що перевіряє:</b> п'ять сценаріїв з невалідними аргументами командного рядка —
         * пропущений {@code -k}, пропущений {@code -f}, пропущена команда, невідомий
         * прапорець, нечислове значення ключа. У всіх випадках програма має:
         * (1) не кидати виняток назовні і (2) не створювати жодного нового файлу.
         *
         * <p><b>Як пройти:</b> уже працює — {@code ArgumentsParser} кидає виняток для
         * невалідних аргументів, а {@code Main} цей виняток ловить у {@code try-catch}.
         */
        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', textBlock = """
                missing -k       | -e -f {path}
                missing -f       | -e -k 5
                missing command  | -k 5 -f {path}
                unknown flag     | -e -x -k 5 -f {path}
                non-numeric key  | -e -k abc -f {path}
                """)
        @DisplayName("Невалідні аргументи: програма не падає і не створює зайвих файлів")
        void invalidArgsHandled(String scenario, String argsSpec) {
            String[] args = argsSpec.trim()
                    .replace("{path}", inputFilePathEN.toString())
                    .split("\\s+");
            List<Path> before = listFiles(tempDir);
            assertDoesNotThrow(() -> Main.main(args), scenario);
            assertEquals(before, listFiles(tempDir), scenario);
        }
    }
}
