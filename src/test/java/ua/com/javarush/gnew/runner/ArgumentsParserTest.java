package ua.com.javarush.gnew.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юніт-тести для {@link ArgumentsParser}. Перевіряють обидва шляхи: успішний розбір
 * аргументів і коректне кидання винятків при невалідному вводі.
 *
 * <p>Усі тести проходять без додаткової роботи — {@code ArgumentsParser} уже реалізовано.
 * Цей файл слугує демонстрацією повної поведінки парсера для студентів.
 */
class ArgumentsParserTest {

    private final ArgumentsParser parser = new ArgumentsParser();

    @Nested
    @DisplayName("Успішний розбір аргументів")
    class HappyPaths {

        /**
         * <b>Що перевіряє:</b> аргументи у канонічному порядку {@code -e -k <key> -f <path>}
         * розпізнаються правильно: команда, ключ і шлях до файлу.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Канонічний порядок: -e -k <ключ> -f <шлях>")
        void canonicalOrder() {
            RunOptions options = parser.parse(new String[]{"-e", "-k", "5", "-f", "/tmp/foo.txt"});
            assertEquals(Command.ENCRYPT, options.getCommand());
            assertEquals(5, options.getKey());
            assertEquals(Paths.get("/tmp/foo.txt"), options.getFilePath());
        }

        /**
         * <b>Що перевіряє:</b> аргументи можна передавати в будь-якому порядку —
         * наприклад, {@code -f <path> -k <key> -e}.
         *
         * <p><b>Як пройти:</b> уже працює — {@code ArgumentsParser} обходить масив
         * аргументів і реагує на кожен прапорець незалежно від позиції.
         */
        @Test
        @DisplayName("Довільний порядок аргументів")
        void reorderedArgs() {
            RunOptions options = parser.parse(new String[]{"-f", "/tmp/foo.txt", "-k", "5", "-e"});
            assertEquals(Command.ENCRYPT, options.getCommand());
            assertEquals(5, options.getKey());
            assertEquals(Paths.get("/tmp/foo.txt"), options.getFilePath());
        }

        /**
         * <b>Що перевіряє:</b> прапорець {@code -d} розпізнається як {@code Command.DECRYPT}.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Прапорець -d → Command.DECRYPT")
        void decryptCommand() {
            RunOptions options = parser.parse(new String[]{"-d", "-k", "5", "-f", "/tmp/foo.txt"});
            assertEquals(Command.DECRYPT, options.getCommand());
        }

        /**
         * <b>Що перевіряє:</b> прапорець {@code -bf} розпізнається як {@code Command.BRUTEFORCE}.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Прапорець -bf → Command.BRUTEFORCE")
        void bruteforceCommand() {
            RunOptions options = parser.parse(new String[]{"-bf", "-k", "5", "-f", "/tmp/foo.txt"});
            assertEquals(Command.BRUTEFORCE, options.getCommand());
        }

        /**
         * <b>Що перевіряє:</b> від'ємний ключ {@code -k -5} коректно розпізнається.
         *
         * <p><b>Як пройти:</b> уже працює — {@code Integer.parseInt} приймає від'ємні числа.
         */
        @Test
        @DisplayName("Парсер приймає від'ємний ключ")
        void negativeKey() {
            RunOptions options = parser.parse(new String[]{"-e", "-k", "-5", "-f", "/tmp/foo.txt"});
            assertEquals(-5, options.getKey());
        }
    }

    @Nested
    @DisplayName("Обробка помилок")
    class Errors {

        /**
         * <b>Що перевіряє:</b> якщо пропущена команда ({@code -e}, {@code -d} або {@code -bf}),
         * парсер кидає {@link IllegalArgumentException} зі словом «command» у повідомленні.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Пропущена команда → IllegalArgumentException")
        void missingCommand() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-k", "5", "-f", "/tmp/foo.txt"}));
            assertTrue(e.getMessage().toLowerCase().contains("command"),
                    "Expected 'command' in error message, got: " + e.getMessage());
        }

        /**
         * <b>Що перевіряє:</b> пропущений {@code -k} → {@link IllegalArgumentException}
         * з «key» у повідомленні.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Пропущений -k → IllegalArgumentException")
        void missingKey() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-f", "/tmp/foo.txt"}));
            assertTrue(e.getMessage().toLowerCase().contains("key"),
                    "Expected 'key' in error message, got: " + e.getMessage());
        }

        /**
         * <b>Що перевіряє:</b> пропущений {@code -f} → {@link IllegalArgumentException}
         * з «file» у повідомленні.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Пропущений -f → IllegalArgumentException")
        void missingFile() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-k", "5"}));
            assertTrue(e.getMessage().toLowerCase().contains("file"),
                    "Expected 'file' in error message, got: " + e.getMessage());
        }

        /**
         * <b>Що перевіряє:</b> прапорець {@code -k} без наступного значення кидає виняток.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("-k без значення → IllegalArgumentException")
        void keyFlagWithoutValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-f", "/tmp/foo.txt", "-k"}));
        }

        /**
         * <b>Що перевіряє:</b> прапорець {@code -f} без наступного значення кидає виняток.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("-f без значення → IllegalArgumentException")
        void fileFlagWithoutValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-k", "5", "-f"}));
        }

        /**
         * <b>Що перевіряє:</b> нечислове значення ключа ({@code -k abc}) кидає
         * {@link NumberFormatException} (стандартний виняток від {@code Integer.parseInt}).
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Нечислове значення ключа → NumberFormatException")
        void nonNumericKey() {
            assertThrows(NumberFormatException.class,
                    () -> parser.parse(new String[]{"-e", "-k", "abc", "-f", "/tmp/foo.txt"}));
        }

        /**
         * <b>Що перевіряє:</b> невідомий прапорець (наприклад, {@code -x}) кидає
         * {@link IllegalArgumentException} зі словом «unknown» у повідомленні.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Невідомий прапорець → IllegalArgumentException")
        void unknownFlag() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-x", "-k", "5", "-f", "/tmp/foo.txt"}));
            assertTrue(e.getMessage().toLowerCase().contains("unknown"),
                    "Expected 'unknown' in error message, got: " + e.getMessage());
        }
    }
}
