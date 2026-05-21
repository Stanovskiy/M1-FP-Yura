package ua.com.javarush.gnew.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CypherTest {

    private final Cypher cypher = new Cypher();

    @DisplayName("[ENCRYPT] simple shifts")
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

    @DisplayName("[ENCRYPT] key=0 leaves text unchanged")
    @Test
    void keyZeroIsIdentity() {
        assertEquals("Hello, World!", cypher.encrypt("Hello, World!", 0));
    }

    @DisplayName("[ENCRYPT] key=52 (full alphabet cycle) leaves text unchanged")
    @Test
    void fullCycleIsIdentity() {
        assertEquals("Hello", cypher.encrypt("Hello", 52));
    }

    @DisplayName("[ENCRYPT] key=53 equals key=1")
    @Test
    void cycleWrapsAround() {
        assertEquals(cypher.encrypt("Hello", 1), cypher.encrypt("Hello", 53));
    }

    @DisplayName("[ENCRYPT] negative key shifts in the opposite direction")
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

    @DisplayName("[ENCRYPT] non-alphabet characters pass through unchanged")
    @ParameterizedTest(name = "encrypt({0}, 5) = {0}")
    @CsvSource({"'.'", "','", "' '", "'!'", "'?'", "'0'", "'9'"})
    void passThrough(String input) {
        assertEquals(input, cypher.encrypt(input, 5));
    }

    @DisplayName("[ENCRYPT] empty string returns empty string")
    @Test
    void emptyString() {
        assertEquals("", cypher.encrypt("", 5));
    }

    @DisplayName("[ENCRYPT] preserves digit and whitespace alongside letters")
    @Test
    void mixedContent() {
        assertEquals("bcd 123", cypher.encrypt("abc 123", 1));
    }
}
