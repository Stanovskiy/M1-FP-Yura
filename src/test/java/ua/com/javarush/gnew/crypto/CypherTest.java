package ua.com.javarush.gnew.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Юніт-тести для {@link Cypher} — без файлового вводу/виводу, прямі виклики методів.
 * Дають швидкий зворотний зв'язок при роботі над логікою шифрування/розшифрування.
 *
 * <p>Тут також зручно дописувати свої тести для {@code Cypher.decrypt} під час реалізації.
 */
class CypherTest {

    private final Cypher cypher = new Cypher();

    /**
     * <b>Що перевіряє:</b> базовий зсув літер, включно з переходом через межу регістру:
     * A+1=B, a+1=b, A+25=Z, a+25=z, Z+1=a (далі по колу), z+1=A (повний оберт).
     *
     * <p><b>Як пройти:</b> уже працює — {@code Cypher.encrypt} реалізовано.
     * Зверни увагу: 52-символьний алфавіт обертається ЯК ЦІЛЕ — після 'Z' йде 'a', не 'A'.
     */
    @DisplayName("[ШИФРУВАННЯ] Простий зсув літер по 52-символьному алфавіту")
    @ParameterizedTest(name = "encrypt({0}, {1}) = {2}")
    @CsvSource({
            "A, 1, B",
            "a, 1, b",
            "A, 25, Z",
            "a, 25, z",
            "Z, 1, a",
            "z, 1, A"
    })
    void simpleShift(String input, int key, String expected) {
        assertEquals(expected, cypher.encrypt(input, key));
    }

    /**
     * <b>Що перевіряє:</b> ключ 0 не змінює текст (нульовий зсув).
     *
     * <p><b>Як пройти:</b> уже працює.
     */
    @DisplayName("[ШИФРУВАННЯ] Ключ 0 — текст без змін")
    @Test
    void keyZeroIsIdentity() {
        assertEquals("Hello, World!", cypher.encrypt("Hello, World!", 0));
    }

    /**
     * <b>Що перевіряє:</b> ключ 52 = повний оберт по 52-символьному алфавіту (A..Z + a..z),
     * тому текст не змінюється.
     *
     * <p><b>Як пройти:</b> уже працює — {@code Collections.rotate(list, 52)} еквівалентно
     * нульовому зсуву для 52-елементного списку.
     */
    @DisplayName("[ШИФРУВАННЯ] Ключ 52 (повне коло) — текст без змін")
    @Test
    void fullCycleIsIdentity() {
        assertEquals("Hello", cypher.encrypt("Hello", 52));
    }

    /**
     * <b>Що перевіряє:</b> ключ 53 еквівалентний ключу 1 (53 mod 52 = 1).
     * Тобто ключі більші за розмір алфавіту мають нормалізуватись по колу.
     *
     * <p><b>Як пройти:</b> уже працює.
     */
    @DisplayName("[ШИФРУВАННЯ] Ключ 53 дає той самий результат, що ключ 1")
    @Test
    void cycleWrapsAround() {
        assertEquals(cypher.encrypt("Hello", 1), cypher.encrypt("Hello", 53));
    }

    /**
     * <b>Що перевіряє:</b> від'ємні ключі зсувають у зворотному напрямку з циклічним
     * переходом: A-1=z (через увесь алфавіт назад), a-1=Z (через межу регістру),
     * Z-25=A (з 'Z' назад на 25 позицій), z-25=a.
     *
     * <p><b>Як пройти:</b> уже працює — алгоритм коректно обробляє від'ємні ключі.
     */
    @DisplayName("[ШИФРУВАННЯ] Від'ємний ключ зсуває у зворотному напрямку (по колу)")
    @ParameterizedTest(name = "encrypt({0}, {1}) = {2}")
    @CsvSource({
            "A, -1, z",
            "a, -1, Z",
            "Z, -25, A",
            "z, -25, a"
    })
    void negativeKey(String input, int key, String expected) {
        assertEquals(expected, cypher.encrypt(input, key));
    }

    /**
     * <b>Що перевіряє:</b> символи, яких немає в алфавіті (розділові знаки, цифри, пробіли),
     * проходять через шифр без змін.
     *
     * <p><b>Як пройти:</b> уже працює — {@code Cypher.processSymbol} повертає символ без змін,
     * якщо його немає в {@code originalAlphabet}.
     */
    @DisplayName("[ШИФРУВАННЯ] Нелітерні символи проходять без змін")
    @ParameterizedTest(name = "encrypt({0}, 5) = {0}")
    @CsvSource({"'.'", "','", "' '", "'!'", "'?'", "'0'", "'9'"})
    void passThrough(String input) {
        assertEquals(input, cypher.encrypt(input, 5));
    }

    /**
     * <b>Що перевіряє:</b> шифрування порожнього рядка повертає порожній рядок.
     *
     * <p><b>Як пройти:</b> уже працює.
     */
    @DisplayName("[ШИФРУВАННЯ] Порожній рядок — порожній результат")
    @Test
    void emptyString() {
        assertEquals("", cypher.encrypt("", 5));
    }

    /**
     * <b>Що перевіряє:</b> у змішаному вмісті ('abc 123' з ключем 1) літери зсуваються,
     * а пробіл і цифри залишаються незмінними: «abc 123» → «bcd 123».
     *
     * <p><b>Як пройти:</b> уже працює.
     */
    @DisplayName("[ШИФРУВАННЯ] Літери зсуваються, цифри й пробіли — ні")
    @Test
    void mixedContent() {
        assertEquals("bcd 123", cypher.encrypt("abc 123", 1));
    }
}
