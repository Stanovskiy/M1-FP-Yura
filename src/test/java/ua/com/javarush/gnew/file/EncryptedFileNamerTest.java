package ua.com.javarush.gnew.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Юніт-тести для {@link EncryptedFileNamer} — допоміжного класу, що формує імена
 * вихідних файлів для операцій шифрування/розшифрування.
 *
 * <p>Усі тести проходять без додаткової роботи — клас уже реалізовано. Цей файл
 * фіксує правила, за якими формуються імена, щоб майбутні зміни в логіці не
 * зламали поведінку, на яку покладається {@code Main}.
 */
class EncryptedFileNamerTest {

    private final EncryptedFileNamer namer = new EncryptedFileNamer();

    @Nested
    @DisplayName("forEncrypted — ім'я для зашифрованого файлу")
    class ForEncrypted {

        /**
         * <b>Що перевіряє:</b> {@code forEncrypted("foo.txt")} → {@code foo [ENCRYPTED].txt}.
         * Мітка {@code [ENCRYPTED]} вставляється ПЕРЕД розширенням, через пробіл.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Додає ' [ENCRYPTED]' перед розширенням .txt")
        void appendsEncryptedMarker() {
            Path result = namer.forEncrypted(Paths.get("/tmp/foo.txt"));
            assertEquals(Paths.get("/tmp/foo [ENCRYPTED].txt"), result);
        }

        /**
         * <b>Що перевіряє:</b> {@code forEncrypted} зберігає шлях до батьківської директорії
         * — новий файл створюється поряд з оригінальним, у тій самій папці.
         *
         * <p><b>Як пройти:</b> уже працює — використовується {@code Path.resolveSibling(...)}.
         */
        @Test
        @DisplayName("Зберігає шлях до батьківської директорії")
        void preservesParent() {
            Path result = namer.forEncrypted(Paths.get("/a/b/c/file.txt"));
            assertEquals(Paths.get("/a/b/c/file [ENCRYPTED].txt"), result);
        }
    }

    @Nested
    @DisplayName("forDecrypted — ім'я для розшифрованого файлу")
    class ForDecrypted {

        /**
         * <b>Що перевіряє:</b> якщо в імені вже є мітка {@code [ENCRYPTED]}, вона ЗАМІНЮЄТЬСЯ
         * на {@code [DECRYPTED]}, а не додається поряд. Тобто
         * {@code foo [ENCRYPTED].txt} → {@code foo [DECRYPTED].txt}.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Замінює '[ENCRYPTED]' на '[DECRYPTED]'")
        void replacesEncryptedMarker() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo [ENCRYPTED].txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
        }

        /**
         * <b>Що перевіряє:</b> якщо в імені немає мітки {@code [ENCRYPTED]}, мітка
         * {@code [DECRYPTED]} додається перед розширенням. Тобто
         * {@code foo.txt} → {@code foo [DECRYPTED].txt}.
         *
         * <p><b>Як пройти:</b> уже працює.
         */
        @Test
        @DisplayName("Додає ' [DECRYPTED]' перед .txt, якщо мітки немає")
        void appendsWhenNoMarker() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo.txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
        }

        /**
         * <b>Що перевіряє:</b> заборонено отримати ім'я з двома мітками поряд —
         * {@code foo [ENCRYPTED] [DECRYPTED].txt} НЕ має зустрічатися ніколи.
         *
         * <p><b>Як пройти:</b> уже працює — логіка явно ЗАМІНЮЄ {@code [ENCRYPTED]}
         * на {@code [DECRYPTED]}, а не додає другу мітку.
         */
        @Test
        @DisplayName("Не створює дві мітки в одному імені")
        void noDoubleSuffix() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo [ENCRYPTED].txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
        }
    }
}
